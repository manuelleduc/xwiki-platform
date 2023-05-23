/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.requiredrights.internal;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.display.internal.DocumentDisplayer;
import org.xwiki.display.internal.DocumentDisplayerParameters;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.security.SecurityReferenceFactory;
import org.xwiki.security.authorization.Right;
import org.xwiki.security.authorization.cache.SecurityCache;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

import static com.xpn.xwiki.doc.XWikiDocument.CKEY_SDOC;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.security.authorization.Right.PROGRAM;
import static org.xwiki.security.authorization.Right.SCRIPT;

/**
 * @version $Id$
 * @since x.y.z
 */
@Component(roles = AnalysisResultsService.class)
@Singleton
public class AnalysisResultsService
{
    @Inject
    private AnalysisResultsStore analysisResultsStore;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private SecurityReferenceFactory securityReferenceFactory;

    @Inject
    private SecurityCache securityCache;

    @Inject
    private DocumentDisplayer documentDisplayer;

    @Inject
    @Named("context")
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    public void analyse(XWikiDocument document)
    {
        try {
            DocumentReference newDocumentReference = document.getDocumentReference().appendParent(null)
                new DocumentReference(
                    String.format("Test%d-%s", System.currentTimeMillis(), document.getDocumentReference().getName()),
                    document.getDocumentReference().getLastSpaceReference());
            XWikiDocument doc = document.cloneRename(newDocumentReference, this.xcontextProvider.get());
            doc.setRequiredRightsActivated(true);

            boolean errorWithEmptyRequiredRights = hasError(doc, Set.of());
            boolean errorWithScriptRequiredRights = hasError(doc, Set.of(SCRIPT));
            boolean errorWithScriptAndProgramRequiredRights = hasError(doc, Set.of(SCRIPT, PROGRAM));
            if (errorWithEmptyRequiredRights
                || errorWithScriptRequiredRights
                || errorWithScriptAndProgramRequiredRights)
            {
                Set<Right> requiredRights = new HashSet<>();
                if (errorWithEmptyRequiredRights && !errorWithScriptRequiredRights
                    || errorWithEmptyRequiredRights && !errorWithScriptAndProgramRequiredRights)
                {
                    requiredRights.add(SCRIPT);
                }

                if (errorWithScriptRequiredRights && !errorWithScriptAndProgramRequiredRights) {
                    requiredRights.add(PROGRAM);
                }
                this.analysisResultsStore.addResult(doc.getDocumentReference(), doc.getVersion(), requiredRights);
            }
        } catch (RequiredRightException e) {
            this.logger.warn("Failed to saved the analysis results for [{}]. Cause: [{}]",
                document.getDocumentReference(), getRootCauseMessage(e));
        } catch (ComponentLookupException e) {
            this.logger.warn("Failed to render document [{}]. Cause: [{}]",
                document.getDocumentReference(), getRootCauseMessage(e));
        } catch (XWikiException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasError(XWikiDocument doc, Set<Right> requiredRights) throws ComponentLookupException
    {
        doc.getRequiredRights().setRights(requiredRights);
        return cleanAndRender(doc).contains("Check the rights of its last author, the required rights of the document");
    }

    private String cleanAndRender(XWikiDocument doc) throws ComponentLookupException
    {
        this.xcontextProvider.get().put(CKEY_SDOC, doc);
        this.securityCache.remove(this.securityReferenceFactory.newEntityReference(doc.getDocumentReference()));
        DocumentDisplayerParameters parameters = new DocumentDisplayerParameters();
        parameters.setExecutionContextIsolated(false);
        parameters.setTransformationContextIsolated(true);
        // Don't consider isRestricted() here as this could invoke a sheet.
        parameters.setTransformationContextRestricted(false);
        // Render the translated content (matching the current language) using this document's syntax.
//        parameters.setContentTranslated(tdoc != this);
        parameters.setTargetSyntax(doc.getSyntax());
        XDOM display = this.documentDisplayer.display(doc, parameters);
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer blockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, doc.getSyntax().toIdString());
        blockRenderer.render(display, printer);
        return doc.displayDocument(this.xcontextProvider.get());
    }

    public void removeResult(XWikiDocument doc) throws RequiredRightException
    {
        this.analysisResultsStore.removeResult(doc.getDocumentReference());
    }
}
