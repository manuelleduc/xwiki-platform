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
package org.xwiki.user.internal.document.search;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryFilter;
import org.xwiki.query.QueryManager;
import org.xwiki.query.internal.UniqueDocumentFilter;
import org.xwiki.user.ActorSearchResult;
import org.xwiki.user.ActorsSearchProvider;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.query.Query.XWQL;

/**
 * Implementation of {@link ActorsSearchProvider} for the wiki user search.
 */
@Component
@Singleton
@Named("user/document")
public class DocumentUserSearchProvider implements ActorsSearchProvider
{
    private static final String SEARCH_QUERY =
        "from doc.object(XWiki.XWikiUsers) as user "
            + "where lower(doc.name) like :input "
            + "or concat(concat(lower(user.first_name), ' '), lower(user.last_name)) like :input "
            + "order by lower(user.first_name), user.first_name, lower(user.last_name), user.last_name";

    @Inject
    private QueryManager queryManager;

    @Inject
    @Named(UniqueDocumentFilter.HINT)
    private Provider<QueryFilter> uniqueQueryFilter;

    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> xcontext;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> compactSerializer;

    @Override
    public List<ActorSearchResult> search(WikiReference wiki, String input)
    {
        // TODO: currently looking at users from the document store !!!
        XWikiContext xWikiContext = this.xcontext.get();

        try {
            return this.queryManager.createQuery(SEARCH_QUERY, XWQL)
                .setWiki(wiki.getName())
                .bindValue("input", String.format("%%%s%%", input.toLowerCase()))
                .addFilter(this.uniqueQueryFilter.get())
                .<String>execute()
                .stream()
                .map(it -> {
                    DocumentReference documentReference = this.documentReferenceResolver.resolve(it);

                    XWiki xWiki = xWikiContext.getWiki();
                    String label = xWiki.getPlainUserName(documentReference, xWikiContext);
                    String value = this.compactSerializer.serialize(documentReference);
                    String url = xWiki.getURL(documentReference, "view", xWikiContext);
                    return new ActorSearchResult()
                        .setType("user")
                        .setLabel(label)
                        .setValue(value)
                        .setUrl(url);
                })
                .collect(Collectors.toList());
        } catch (QueryException e) {
            this.logger.warn("Failed to create the user search query. Cause: [{}]", getRootCauseMessage(e));
            return emptyList();
        }
    }
}
