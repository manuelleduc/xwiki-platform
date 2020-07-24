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
package org.xwiki.panels.internal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.rendering.async.AsyncContext;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionManager;
import org.xwiki.uiextension.script.UIXPDescriptor;

import static java.util.Collections.emptyList;

/**
 * Abstract panels UI extension manager. Implementations must provide a list of panel IDs to be displayed, this class
 * handles the retrieval of the UI extensions corresponding to the configured panel list.
 *
 * @version $Id$
 * @since 4.3.1
 */
public abstract class AbstractPanelsUIExtensionManager implements UIExtensionManager
{
    /**
     * The default configuration source.
     */
    @Inject
    protected ConfigurationSource configurationSource;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Resolver allowing to retrieve reference from the panels configuration.
     */
    @Inject
    @Named("currentmixed")
    private DocumentReferenceResolver<String> resolver;

    /**
     * We use the Context Component Manager to lookup UI Extensions registered as components. The Context Component
     * Manager allows Extensions to be registered for a specific user, for a specific wiki or for a whole farm.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Inject
    private AsyncContext asyncContext;

    /**
     * Method returning the list of configured panels.
     *
     * @return a comma separated list of panel IDs
     */
    protected abstract String getConfiguration();

    @Override
    public List<UIExtension> get(String extensionPointId)
    {
        // TODO: adapt to aliases.
        List<UIExtension> panels = new ArrayList<>();

        String panelConfigurationString = getConfiguration();

        // Verify that there's a panel configuration property defined, and if not don't return any panel extension.
        if (!StringUtils.isEmpty(panelConfigurationString)) {
            // we store the document reference along with their position in the list,
            // as we want to build a list ordered the same way than in the original panelConfigurationString
            Map<DocumentReference, Integer> panelReferenceWithPosition = new HashMap<>();

            String[] panelStringReferences = panelConfigurationString.split(",");
            for (int i = 0; i < panelStringReferences.length; i++) {
                panelReferenceWithPosition.put(resolver.resolve(panelStringReferences[i].trim()), i);
            }

            try {
                List<UIExtension> allExtensions =
                    contextComponentManagerProvider.get().getInstanceList(UIExtension.class);
                Map<UIExtension, Integer> panelsPositions = new HashMap<>();
                // TODO: This is not performant and will not scale well when the number of UIExtension instances
                // increase in the wiki
                for (UIExtension extension : allExtensions) {
                    DocumentReference extensionId;

                    // We differentiate UIExtension implementations:
                    //
                    // - PanelWikiUIExtension and WikiUIExtension (i.e. WikiComponent): They point to a wiki page and we
                    // can use that page's reference.
                    //
                    // - For other implementations, we only support instance that have their id containing a document
                    // reference.
                    if (extension instanceof WikiComponent) {
                        WikiComponent wikiComponent = (WikiComponent) extension;
                        extensionId = wikiComponent.getDocumentReference();
                    } else {
                        extensionId = resolver.resolve(extension.getId());
                    }

                    if (panelReferenceWithPosition.containsKey(extensionId)) {
                        panelsPositions.put(extension, panelReferenceWithPosition.get(extensionId));
                    }
                }

                panels.addAll(panelsPositions.keySet());
                panels.sort(Comparator.comparing(panelsPositions::get));

                // Indicate that any currently running asynchronous execution result should be removed from the cache as
                // soon as a UIExtension component is modified
                this.asyncContext.useComponent(UIExtension.class);
            } catch (ComponentLookupException e) {
                this.logger.error("Failed to lookup Panels instances, error: [{}]", e);
            }
        }

        return panels;
    }

    @Override
    public List<UIXPDescriptor> getUIXPDescriptors(Long offset, Long limit, String idFilter,
        String filter, String mainIdFilter,
        String aliasesFilter)
    {
        // TODO: log that it should not be called?
        return emptyList();
    }

    @Override
    public long getUIXPDescriptorsTotal(String mainIdFilter, String aliasesFilter)
    {
        return 0;
    }
}
