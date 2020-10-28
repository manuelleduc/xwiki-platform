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
package org.xwiki.user.internal.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.user.ActorSearchResult;
import org.xwiki.user.ActorSearchResults;
import org.xwiki.user.ActorsSearchProvider;
import org.xwiki.user.ActorsSearchService;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Default implementation of {@link ActorsSearchService}.
 * <p>
 * Search for the components with the required hints, call them and collect their results.
 *
 * @version $Id$
 * @since 12.10RC1
 */
@Component
@Singleton
public class DefaultActorsSearchService implements ActorsSearchService
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    @Override
    public ActorSearchResults search(String uorgs, WikiReference wiki, String input, long limit)
    {
        try {
            List<ActorSearchResult> results = filterProviders(parseUorgsToHints(uorgs))
                .stream()
                .flatMap(it -> {
                    List<ActorSearchResult> search = it.search(wiki, input);
                    if (search != null) {
                        return search.stream();
                    } else {
                        return Stream.of();
                    }
                })
                .sorted(Comparator.comparing(ActorSearchResult::getLabel))
                .limit(limit)
                .collect(Collectors.toList());
            return new ActorSearchResults(results);
            // TODO: sort and limit
        } catch (ComponentLookupException e) {
            this.logger.warn("Failed to retrieve the search providers. Cause: [{}].", getRootCauseMessage(e));
            return new ActorSearchResults();
        }
    }

    private Collection<ActorsSearchProvider> filterProviders(List<String> hints) throws ComponentLookupException
    {
        Map<String, ActorsSearchProvider> instanceMap =
            this.componentManager.getInstanceMap(ActorsSearchProvider.class);
        Collection<ActorsSearchProvider> ret;
        if (hints.isEmpty()) {
            ret = instanceMap.values();
        } else {
            ret = instanceMap.entrySet().stream()
                .filter(it -> hints.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        }
        return ret;
    }

    private List<String> parseUorgsToHints(String uorgs)
    {
        List<String> hints = splitHints(uorgs);
        if (hints.size() == 1 && Objects.equals(hints.get(0), "*")) {
            return emptyList();
        } else if (hints.isEmpty()) {
            return singletonList("group");
        }
        return hints;
    }

    /**
     * Split the string by the comas, removing unwanted spaces.
     *
     * @param string the string to split
     * @return the splitted list
     */
    private List<String> splitHints(String string)
    {
        if (string == null) {
            return Collections.emptyList();
        }
        String[] split = string.trim().split("\\s*,\\s*");
        return Arrays.stream(split)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }
}
