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
package org.xwiki.javascript.importmap.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.Extension;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.javascript.importmap.internal.parser.ImportmapPathDescriptor;
import org.xwiki.javascript.importmap.internal.parser.JavascriptImportmapException;
import org.xwiki.javascript.importmap.internal.parser.JavascriptImportmapParser;
import org.xwiki.model.namespace.WikiNamespace;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.webjars.WebJarsUrlFactory;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.javascript.importmap.internal.parser.JavascriptImportmapParser.JAVASCRIPT_IMPORTMAP_PROPERTY;
import static org.xwiki.rendering.syntax.Syntax.HTML_5_0;

/**
 * Resolve the importmap from the webjar declarations, using the
 * {@link JavascriptImportmapParser#JAVASCRIPT_IMPORTMAP_PROPERTY} pom property.
 *
 * @version $Id$
 * @since 18.0.0RC1
 */
@Component(roles = JavascriptImportmapResolver.class)
@Singleton
public class JavascriptImportmapResolver
{
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        // Keeps the output deterministic.
        .configure(SORT_PROPERTIES_ALPHABETICALLY, true)
        .build();

    private static final JavascriptImportmapParser JAVASCRIPT_IMPORTMAP_PARSER = new JavascriptImportmapParser();

    /**
     * Cache lock to prevent race conditions when retrieving cached values.
     */
    private final Object cacheLock = new Object();

    /**
     * The cached value, concurrent operation on this field must use the {@link #cacheLock} object for synchronization.
     */
    private Block cachedValue;

    @Inject
    private Logger logger;

    @Inject
    private InstalledExtensionRepository installedExtensionRepository;

    @Inject
    private CoreExtensionRepository coreExtensionRepository;

    @Inject
    private WebJarsUrlFactory webJarsUrlFactory;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    /**
     * @return the block to render from the importmap, possibly cached
     */
    public Block getBlock()
    {
        synchronized (this.cacheLock) {
            if (this.cachedValue == null) {
                compute();
            }
            return this.cachedValue;
        }
    }

    /**
     * Clear the cache of the resolver.
     */
    public void clearCache()
    {
        synchronized (this.cacheLock) {
            this.cachedValue = null;
        }
    }

    private record ResolveMapEnum(String url, boolean eager, boolean anonymous)
    {
    }

    private void compute()
    {
        var wikiNamespace = new WikiNamespace(this.wikiDescriptorManager.getCurrentWikiId()).serialize();
        List<Map<String, ResolveMapEnum>> extensionsWithImportMap = Stream.concat(
                this.installedExtensionRepository.getInstalledExtensions(wikiNamespace).stream(),
                this.coreExtensionRepository.getCoreExtensions().stream())
            .filter(extension -> accessProperty(extension) != null)
            .map(extension -> {
                String importMapJSON = accessProperty(extension);
                Map<String, ResolveMapEnum> extensionImportMap;
                try {
                    extensionImportMap = JAVASCRIPT_IMPORTMAP_PARSER.parse(importMapJSON)
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> {
                                ImportmapPathDescriptor descriptor = e.getValue();
                                return new ResolveMapEnum(this.webJarsUrlFactory.url(descriptor.descriptor()),
                                    descriptor.eager(), descriptor.anonymous());
                            }
                        ));
                } catch (JavascriptImportmapException e) {
                    this.logger.warn("Unable to read property [{}] for extension [{}]. Cause: [{}]",
                        JAVASCRIPT_IMPORTMAP_PROPERTY, extension.getId(), getRootCauseMessage(e));
                    extensionImportMap = Map.of();
                }
                return extensionImportMap;
            })
            .toList();

        Map<String, String> namedResolvedMap = new HashMap<>();
        Map<String, String> eagerResolvedMap = new HashMap<>();
        for (Map<String, ResolveMapEnum> objectObjectMap : extensionsWithImportMap) {
            for (Map.Entry<String, ResolveMapEnum> objectObjectEntry : objectObjectMap.entrySet()) {
                computeNamed(objectObjectEntry, namedResolvedMap);
                computeEager(objectObjectEntry, eagerResolvedMap);
            }
        }

        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(Map.of("imports", namedResolvedMap));
        } catch (JsonProcessingException e) {
            this.logger.warn("Failed to serialize the importmap. Cause: [{}]", getRootCauseMessage(e));
            json = "{}";
        }

        var eagers =
            eagerResolvedMap.values().stream().map("""
                    <script type="module" src="%s"></script>"""::formatted)
                .collect(Collectors.joining(System.lineSeparator()));

        this.cachedValue =
            new RawBlock("<script type='importmap'>%s</script>%s".formatted(json, eagerResolvedMap.isEmpty()
                ? "" : System.lineSeparator() + eagers), HTML_5_0);
    }

    private void computeNamed(Map.Entry<String, ResolveMapEnum> objectObjectEntry, Map<String, String> namedResolvedMap)
    {
        String key = objectObjectEntry.getKey();
        ResolveMapEnum value = objectObjectEntry.getValue();
        String existingValue = namedResolvedMap.get(key);
        if (existingValue == null && !value.anonymous) {
            namedResolvedMap.put(key, value.url);
        } else if (existingValue != null && !Objects.equals(value.url, existingValue)) {
            this.logger.warn(
                "Conflicting importmap resolution for key [{}]. Existing value: [{}], new value: [{}]",
                key, existingValue, value.url);
        }
    }

    private void computeEager(Map.Entry<String, ResolveMapEnum> objectObjectEntry,
        Map<String, String> eager)
    {
        String key = objectObjectEntry.getKey();
        ResolveMapEnum value = objectObjectEntry.getValue();
        String existingValue = eager.get(key);
        if (existingValue == null && value.eager) {
            eager.put(key, value.url);
        } else if (existingValue != null && !Objects.equals(value.url, existingValue)) {
            this.logger.warn(
                "Conflicting eager resolution for key [{}]. Existing value: [{}], new value: [{}]",
                key, existingValue, value.url);
        }
    }

    private static String accessProperty(Extension extension)
    {
        return extension.getProperty(JAVASCRIPT_IMPORTMAP_PROPERTY);
    }
}
