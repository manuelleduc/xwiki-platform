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
package org.xwiki.tag.internal.selector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryFilter;
import org.xwiki.query.internal.HiddenDocumentFilter;
import org.xwiki.query.internal.UniqueDocumentFilter;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.tag.internal.TagException;
import org.xwiki.tag.internal.TagsSelector;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.tag.TagPlugin;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static org.xwiki.security.authorization.Right.VIEW;

/**
 * @version $Id$
 * @since x.y.z
 */
@Component
@Named("random")
public class RandomCheckTagsSelector extends AbstractTagsSelector implements Initializable
{
    /**
     * Hint for this component.
     */
    public static final String HINT = "exhaustive";

    private Random random;

    @Override
    public void initialize() throws InitializationException
    {
        this.random = new Random();
    }

    @Override
    public List<String> getAllTags() throws TagException
    {
        String hql = "select distinct doc.fullName, elements(prop.list) as tag"
            + "from XWikiDocument as doc, BaseObject as obj, DBStringListProperty as prop "
            + "where obj.name=doc.fullName "
            + "and obj.className='XWiki.TagClass' "
            + "and obj.id=prop.id.id "
            + "and prop.id.name='tags' "
            + "order by tag";

        try {
            List<Object[]> results = this.contextProvider.get()
                .getWiki()
                .getStore()
                .getQueryManager()
                .createQuery(hql, Query.HQL)
                .addFilter(this.hiddenDocumentQueryFilter)
                .execute();
            return computedTagsFromQuery(results);
        } catch (QueryException e) {
            throw new TagException(String.format("Failed to get all tags for query [%s]", hql), e);
        }
    }

    @Override
    public Map<String, Integer> getTagCountForQuery(String fromHql, String whereHql, List<?> parameterValues)
        throws TagException
    {
        return getTagsFromViewableDocuments(fromHql, whereHql, parameterValues);
    }

    private List<String> computedTagsFromQuery(List<Object[]> results)
    {
        Set<String> tagsSet = new HashSet<>();
        // TODO: aggregating this list can be expensive if a tag is present on a large amount of pages.
        List<String> aggregatedDocs = new ArrayList<>();
        String previousTag = null;
        for (Object[] cols : results) {
            String documentReferenceStr = (String) cols[0];
            String tag = (String) cols[1];
            // Since the documents are sorted by their document reference, we know that we have to re-compute the 
            // rights only for the first result, or when we pass to the next document reference.
            if (previousTag == null) {
                aggregatedDocs.add(documentReferenceStr);
                previousTag = tag;
            } else if (!Objects.equals(previousTag, tag)) {
                String randomDocumentReference = aggregatedDocs.get(this.random.nextInt(aggregatedDocs.size()));

                if (this.contextualAuthorizationManager
                    .hasAccess(VIEW, this.stringDocumentReferenceResolver.resolve(randomDocumentReference)))
                {
                    tagsSet.add(randomDocumentReference);
                }

                aggregatedDocs.clear();
                aggregatedDocs.add(documentReferenceStr);
                previousTag = tag;
            } else {
                aggregatedDocs.add(documentReferenceStr);
            }
        }

        List<String> tagsList = new ArrayList<>(tagsSet);
        tagsList.sort(CASE_INSENSITIVE_ORDER);
        return tagsList;
    }

    @Override
    public Map<String, Integer> getTagCountForQuery(String fromHql, String whereHql, Map<String, ?> parameters)
        throws TagException
    {
        return getTagsFromViewableDocuments(fromHql, whereHql, parameters);
    }

    private Map<String, Integer> getTagsFromViewableDocuments(String fromHql, String whereHql, Object parameters)
        throws TagException
    {
        String from =
            "select distinct doc.fullName as fullName, elements(prop.list) as tag "
                + "from XWikiDocument as doc, BaseObject as tagobject, DBStringListProperty as prop";
        String where = " where tagobject.name=doc.fullName and tagobject.className='XWiki.TagClass' and "
            + "tagobject.id=prop.id.id and prop.id.name='tags' and doc.translation=0";

        // If at least one of the fragments is passed, the query should be matching XWiki documents
        if (!StringUtils.isBlank(fromHql) || !StringUtils.isBlank(whereHql)) {
            from += fromHql;
        }
        if (!StringUtils.isBlank(whereHql)) {
            where += " and " + whereHql;
        }

        String hql = from + where + " order by tag";

        try {
            Query query = this.contextProvider.get()
                .getWiki()
                .getStore()
                .getQueryManager()
                .createQuery(hql, Query.HQL)
                .addFilter(this.hiddenDocumentQueryFilter);
            if (parameters != null) {
                if (parameters instanceof Map) {
                    query.bindValues((Map) parameters);
                } else {
                    query.bindValues((List) parameters);
                }
            }

            return computeCountsFromQuery(query.execute());
        } catch (QueryException e) {
            throw new TagException(
                String.format("Failed to get tag count for query [%s], with parameters [%s]", hql, parameters), e);
        }
    }

    private Map<String, Integer> computeCountsFromQuery(List<Object[]> results)
    {
        Map<String, Integer> mapTagCount = new HashMap<>();
        // TODO: aggregating this list can be expensive if a tag is present on a large amount of pages.
        List<String> aggregatedDocs = new ArrayList<>();
        String previousTag = null;
        for (Object[] cols : results) {
            String documentReferenceStr = (String) cols[0];
            String tag = (String) cols[1];
            // Since the documents are sorted by their document reference, we know that we have to re-compute the 
            // rights only for the first result, or when we pass to the next document reference.
            if (previousTag == null) {
                aggregatedDocs.add(documentReferenceStr);
                previousTag = tag;
            } else if (!Objects.equals(previousTag, tag)) {
                String randomDocumentReference = aggregatedDocs.get(this.random.nextInt(aggregatedDocs.size()));

                if (this.contextualAuthorizationManager
                    .hasAccess(VIEW, this.stringDocumentReferenceResolver.resolve(randomDocumentReference)))
                {
                    mapTagCount.put(previousTag, aggregatedDocs.size());
                }

                aggregatedDocs.clear();
                aggregatedDocs.add(documentReferenceStr);
                previousTag = tag;
            } else {
                aggregatedDocs.add(documentReferenceStr);
            }
        }
        return mapTagCount;
    }
}
