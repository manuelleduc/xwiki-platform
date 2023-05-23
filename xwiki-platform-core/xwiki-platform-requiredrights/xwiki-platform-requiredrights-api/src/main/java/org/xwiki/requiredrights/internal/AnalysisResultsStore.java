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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.security.authorization.Right;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiHibernateStore;

import static org.xwiki.requiredrights.internal.RequiredRightsResultsMandatoryDocumentInitializer.LOCAL_DOCUMENT_REFERENCE;
import static org.xwiki.requiredrights.internal.RequiredRightsResultsMandatoryDocumentInitializer.REFERENCE_FIELD;
import static org.xwiki.requiredrights.internal.RequiredRightsResultsMandatoryDocumentInitializer.REQUIRED_RIGHTS_FIELD;
import static org.xwiki.requiredrights.internal.RequiredRightsResultsMandatoryDocumentInitializer.VERSION_FIELD;

/**
 * @version $Id$
 * @since x.y.z
 */
@Component(roles = AnalysisResultsStore.class)
@Singleton
public class AnalysisResultsStore extends XWikiHibernateStore
{
    private static final LocalDocumentReference REFERENCE =
        new LocalDocumentReference(List.of("XWiki", "RequiredRights", "Code"), "RequiredRightsResult");

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> localSerializer;

    public void removeResult(DocumentReference documentReference) throws RequiredRightException
    {
        XWikiContext context = this.xcontextProvider.get();
        XWiki wiki = context.getWiki();
        try {
            XWikiDocument document1 = wiki.getDocument(REFERENCE, context);
            XWikiDocument document = internalRemove(documentReference, document1);
            wiki.saveDocument(document, String.format("Remove [%s] from the required rights results.", document),
                context);
        } catch (XWikiException e) {
            throw new RequiredRightException("Failed to get document [" + documentReference + "]", e);
        }
    }

    public void addResult(DocumentReference documentReference, String version, Set<Right> requiredRights)
        throws RequiredRightException
    {

        XWikiContext context = this.xcontextProvider.get();
        XWiki wiki = context.getWiki();
        try {
            XWikiDocument document = wiki.getDocument(REFERENCE, context);
            document.setRequiredRightsActivated(true);
            String reference = this.localSerializer.serialize(documentReference);
            List<String> rights = requiredRights.stream().map(Right::getName).collect(Collectors.toList());
            updateAndSave(documentReference, version, rights, context, wiki, document, reference);
        } catch (XWikiException e) {
            throw new RequiredRightException(
                String.format("Failed created result for  [%s] version [%s] and required rights [%s]",
                    documentReference, version, requiredRights), e);
        }
    }

    private synchronized void updateAndSave(DocumentReference documentReference, String version,
        List<String> requiredRights, XWikiContext context, XWiki wiki, XWikiDocument document, String reference)
        throws XWikiException
    {
        internalRemove(documentReference, document);
        BaseObject baseObject = document.newXObject(LOCAL_DOCUMENT_REFERENCE, context);
        baseObject.setStringValue(REFERENCE_FIELD, reference);
        baseObject.setStringValue(VERSION_FIELD, version);
        baseObject.setStringListValue(REQUIRED_RIGHTS_FIELD, requiredRights);

        String commitMessage =
            String.format("Add [%s] version [%s] with required rights [%s]", documentReference, version,
                requiredRights);
        wiki.saveDocument(document, commitMessage, context);
    }

    private XWikiDocument internalRemove(DocumentReference documentReference, XWikiDocument document)
    {
        String serialize = this.localSerializer.serialize(documentReference);
        for (BaseObject xObject : document.getXObjects(LOCAL_DOCUMENT_REFERENCE)) {
            if (Objects.equals(xObject.getStringValue(REFERENCE_FIELD), serialize)) {
                document.removeXObject(xObject);
            }
        }
        return document;
    }
}
