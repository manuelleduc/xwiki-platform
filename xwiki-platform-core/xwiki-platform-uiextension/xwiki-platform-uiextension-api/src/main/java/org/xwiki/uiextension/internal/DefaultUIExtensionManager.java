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
package org.xwiki.uiextension.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.async.AsyncContext;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionManager;
import org.xwiki.uiextension.script.UIXPDescriptor;

import com.xpn.xwiki.internal.skin.InternalSkinManager;

import static org.xwiki.query.Query.HQL;

/**
 * Default UIExtensionManager, retrieves all the extensions for a given extension point.
 *
 * @version $Id$
 * @since 4.3.1
 */
@Component
@Singleton
public class DefaultUIExtensionManager implements UIExtensionManager
{
    private static final String MAIN_ID_BINDING = "mainIdFilter";

    private static final String ALIASES_FILTER_BINDING = "aliasesFilter";

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    @Inject
    private QueryManager queryManager;

    /**
     * We use the Context Component Manager to lookup UI Extensions registered as components. The Context Component
     * Manager allows Extensions to be registered for a specific user, for a specific wiki or for a whole farm.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Inject
    private AsyncContext asyncContext;

    @Inject
    private InternalSkinManager internalSkinManager;

    @Override
    public List<UIExtension> get(String extensionPointId)
    {
        Set<String> extensionPointIds = resolveUIXPAliases(extensionPointId);
        return innerGet(extensionPointIds);
    }

    private Set<String> resolveUIXPAliases(String uixpId)
    {
        // look for aliases in UIExtensionPointDescriptorClass object
        Set<String> aliases = lookupAlias(uixpId);

        // look for aliases in properties if not found in UIExtensionPointDescriptorClass objects
        // TODO: should we merge the aliases of just ignire them when also defined in a UIExtensionPointDescriptorClass?
        if (aliases == null || aliases.isEmpty()) {
            aliases = this.internalSkinManager.getAliases(uixpId);
        }

        // use the extentions point alone without aliases
        if (aliases == null) {
            aliases = new HashSet<>();
        }

        // add the request uixp id to the set
        aliases.add(uixpId);

        return aliases;
    }

    private List<UIExtension> innerGet(Set<String> extensionPointIds)
    {
        List<UIExtension> extensions = new ArrayList<>();

        try {
            List<UIExtension> allExtensions =
                this.contextComponentManagerProvider.get().getInstanceList(UIExtension.class);
            for (UIExtension extension : allExtensions) {
                if (extensionPointIds.contains(extension.getExtensionPointId())) {
                    extensions.add(extension);
                }
            }

            // Indicate that any currently running asynchronous execution result should be removed from the cache as
            // soon as a UIExtension component is modified
            this.asyncContext.useComponent(UIExtension.class);
        } catch (ComponentLookupException e) {
            this.logger.error("Failed to lookup UIExtension instances, error: [{}]", e);
        }

        return extensions;
    }

    @Override
    public List<UIXPDescriptor> getUIXPDescriptors(Long offset, Long limit, String mainIdFilter,
        String aliasesFilter, String sort,
        String dir)
    {
        return getUIXPDescriptors(mainIdFilter, aliasesFilter, false, sort, dir, offset, limit);
    }

    private List<UIXPDescriptor> getUIXPDescriptors(String mainIdFilter, String aliasesFilter, boolean exactMatch,
        String sort, String dir, Long offset, Long limit)
    {
        String queryString =
            "SELECT string.value, largestring.value "
                + "FROM BaseObject xobject "
                + "INNER JOIN LargeStringProperty largestring ON largestring.id = xobject.id "
                + "INNER JOIN StringProperty string ON string.id = xobject.id ";
        List<String> wheres = new ArrayList<>();
        Map<String, Object> bindings = new HashMap<>();
        wheres.add("xobject.className = 'XWiki.UIExtensionPointDescriptorClass'");
        if (StringUtils.isNotEmpty(mainIdFilter)) {
            if (exactMatch) {

                wheres.add(String.format("string.value = :%s", MAIN_ID_BINDING));
                bindings.put(MAIN_ID_BINDING, mainIdFilter);
            } else {
                wheres.add(String.format("string.value LIKE :%s", MAIN_ID_BINDING));
                bindings.put(MAIN_ID_BINDING, '%' + mainIdFilter + '%');
            }
        }
        if (StringUtils.isNotEmpty(aliasesFilter)) {
            wheres.add(String.format("largestring.value LIKE :%s", ALIASES_FILTER_BINDING));
            bindings.put(ALIASES_FILTER_BINDING, '%' + aliasesFilter + '%');
        }

        if (!wheres.isEmpty()) {
            queryString += "WHERE " + StringUtils.join(wheres, " AND ");
        }

        if (sort.equals("mainId")) {
            queryString += " ORDER BY ";
            queryString += "string.value";
            if (!dir.equals("asc")) {
                queryString += " DESC";
            }
        }

        List<UIXPDescriptor> ret = new ArrayList<>();

        try {
            Query query = this.queryManager.createQuery(queryString, HQL);
            if (limit != null && offset != null) {
                // todo: change parameter type to int.
                query.setLimit(Math.toIntExact(limit))
                    .setOffset(Math.toIntExact(offset - 1));
            }
            query.bindValues(bindings);
            List<Object[]> execute = query.execute();
            for (Object[] o : execute) {
                ret.add(new UIXPDescriptor()
                            .setMainId((String) o[0])
                            .setAliases(parseAliases((String) o[1])));
            }
        } catch (QueryException e) {
            // TODO: log
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public long getUIXPDescriptorsTotal(String mainIdFilter, String aliasesFilter)
    {
        //language=HQL
        String queryString =
            "SELECT count(xobject.id) "
                + "FROM BaseObject xobject "
                + "INNER JOIN LargeStringProperty largestring ON largestring.id = xobject.id "
                + "INNER JOIN StringProperty string ON string.id = xobject.id ";
        List<String> wheres = new ArrayList<>();
        Map<String, Object> bindings = new HashMap<>();
        wheres.add("xobject.className = 'XWiki.UIExtensionPointDescriptorClass'");
        if (StringUtils.isNotEmpty(mainIdFilter)) {
            wheres.add(String.format("string.value LIKE :%s", MAIN_ID_BINDING));
            bindings.put(MAIN_ID_BINDING, '%' + mainIdFilter + '%');
        }
        if (StringUtils.isNotEmpty(aliasesFilter)) {
            wheres.add(String.format("largestring.value LIKE :%s", ALIASES_FILTER_BINDING));
            bindings.put(ALIASES_FILTER_BINDING, '%' + aliasesFilter + '%');
        }

        if (!wheres.isEmpty()) {
            queryString += "WHERE " + StringUtils.join(wheres, " AND ");
        }

        try {
            Query query = this.queryManager.createQuery(queryString, HQL);
            query.bindValues(bindings);
            return query.<Long>execute().get(0);
        } catch (QueryException e) {
            // TODO: log
            e.printStackTrace();
        }
        return 0L;
    }

    private Set<String> lookupAlias(String uixpId)
    {
        List<UIXPDescriptor> uixpDescriptors = this.getUIXPDescriptors(uixpId, null, true, "mainId", "asc", null, null);

        if (uixpDescriptors.size() > 1) {
            // TODO: log error, too many declarations
        }
        Set<String> ret = new HashSet<>();

        if (!uixpDescriptors.isEmpty()) {
            UIXPDescriptor uixpDescriptor = uixpDescriptors.get(0);
            ret.add(uixpDescriptor.getMainId());
            ret.addAll(uixpDescriptor.getAliases());
        }
        return ret;
    }

    private Set<String> parseAliases(String value)
    {
        Set<String> ret;
        String[] aliases = value
                               .split("\\r?\\n");
        ret = Arrays.stream(aliases)
                  .map(String::trim)
                  .filter(it -> !StringUtils.isBlank(it))
                  .collect(Collectors.toSet());
        return ret;
    }
}
