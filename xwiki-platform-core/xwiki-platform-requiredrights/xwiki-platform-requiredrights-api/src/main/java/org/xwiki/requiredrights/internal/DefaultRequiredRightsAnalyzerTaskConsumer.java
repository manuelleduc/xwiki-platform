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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.index.IndexException;
import org.xwiki.index.TaskConsumer;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 * @since x.y.z
 */
@Component
@Singleton
@Named(DefaultRequiredRightsAnalyzerTaskConsumer.ID)
public class DefaultRequiredRightsAnalyzerTaskConsumer implements TaskConsumer
{
    /**
     * Component hint.
     */
    public static final String ID = "requiredRightsAnalysis";

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private AnalysisResultsService analysisResultsService;

    @Override
    public void consume(DocumentReference documentReference, String version) throws IndexException
    {
        try {
            XWikiContext context = this.xcontextProvider.get();
            // Get the latest version of the document for analysis.
            this.analysisResultsService.analyse(context.getWiki().getDocument(documentReference, context));
        } catch (XWikiException e) {
            throw new IndexException(String.format("Failed to analyze required rights for document [%s] version [%s].",
                documentReference, version), e);
        }
    }
}
