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
package org.xwiki.image.style.internal;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.image.style.ImageStyleException;
import org.xwiki.image.style.ImageStyleManager;
import org.xwiki.image.style.model.ImageStyle;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * TODO: document me.
 *
 * @version $Id$
 * @since 14.2RC1
 */
@Component
@Singleton
public class DefaultImageStyleManager implements ImageStyleManager
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private ExecutionContextManager contextManager;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Override
    public Set<ImageStyle> getImageStyles(String wikiName) throws ImageStyleException
    {
        try {
            this.contextManager.pushContext(new ExecutionContext(), true);
            this.xcontextProvider.get().setWikiId(wikiName);
            return this.queryManager.createQuery("from doc.object(Image.Style.Code.ImageStyleClass) as imageStyle",
                    Query.XWQL)
                .setWiki(wikiName)
                .<String>execute()
                .stream()
                .map(this::convert)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        } catch (QueryException e) {
            throw new ImageStyleException("Failed to retrieve the list of image styles", e);
        } catch (ExecutionContextException e) {
            throw new ImageStyleException(String.format("Failed to initialize a context for wiki [%s]", wikiName), e);
        } finally {
            this.contextManager.popContext();
        }
    }

    private ImageStyle convert(String documentReference)
    {
        try {
            XWikiContext context = this.xcontextProvider.get();
            DocumentReference resolve = this.documentReferenceResolver.resolve(documentReference);
            XWikiDocument document = context.getWiki().getDocument(resolve, context);
            LocalDocumentReference classReference =
                new LocalDocumentReference(List.of("Image", "Style", "Code"), "ImageStyleClass");
            BaseObject xObject = document.getXObject(classReference);
            return new ImageStyle()
                .setType(xObject.getStringValue("type"))
                .setIdentifier(document.getDocumentReference().getName())
                .setPrettyName(xObject.getStringValue("prettyName"));
        } catch (XWikiException e) {
            // TODO: log
            return null;
        }
    }
    
    
}
