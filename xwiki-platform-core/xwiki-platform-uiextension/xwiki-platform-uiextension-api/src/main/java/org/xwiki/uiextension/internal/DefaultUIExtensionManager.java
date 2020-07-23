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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.async.AsyncContext;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.skin.InternalSkinManager;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.LargeStringProperty;

import static com.xpn.xwiki.XWiki.SYSTEM_SPACE;
import static org.xwiki.query.Query.XWQL;

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
    private static final String UIXP_DESCRIPTOR_ALIASID_FIELD = "aliasIds";

    private static final String UIXP_CLASS_NAME = "UIExtensionPointDescriptorClass";

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    @Inject
    private QueryManager queryManager;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

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

    private Set<String> lookupAlias(String uixpId)
    {
        String queryString =
            String.format("from doc.object(%s.%s) as uixp where uixp.mainId = :mainId", SYSTEM_SPACE, UIXP_CLASS_NAME);
        Set<String> ret = null;
        try {
            Query query = this.queryManager.createQuery(queryString, XWQL);
            query.bindValue("mainId", uixpId);
            List<String> documentReferences = query.execute();
            if (documentReferences.size() > 1) {
                String ls = System.lineSeparator();
                this.logger.warn(String.format(
                    "More than one document is holding a [%s.%s] with mainId = [%s]. Found documents: [%s%s%s]",
                    SYSTEM_SPACE, UIXP_CLASS_NAME, uixpId, ls,
                    documentReferences.stream().map(it -> "- " + it).collect(Collectors.joining(ls)), ls));
            }

            if (documentReferences.size() > 0) {

                XWikiContext context = this.xcontextProvider.get();
                XWiki xWiki = context.getWiki();

                String docName = documentReferences.get(0);
                DocumentReference documentReference = this.documentReferenceResolver.resolve(docName);
                try {
                    XWikiDocument document = xWiki.getDocument(documentReference, context);
                    List<BaseObject> xObjects =
                        document.getXObjects(new DocumentReference(context.getWikiId(), SYSTEM_SPACE, UIXP_CLASS_NAME));
                    if (xObjects.size() > 1) {
                        String ls = System.lineSeparator();
                        this.logger.warn(String.format(
                            "More than one XObject of type [%s.%s] with mainId = [%s] found in Document [%s]. "
                                + "Found XObjects: [%s%s%s]",
                            SYSTEM_SPACE, UIXP_CLASS_NAME, uixpId, documentReference, ls,
                            xObjects.stream().map(it -> "-" + it).collect(Collectors.joining(ls)), ls));
                    }

                    BaseObject descriptor = xObjects.get(0);
                    Stream<String> stream =
                        Arrays.stream(((LargeStringProperty) descriptor.get(UIXP_DESCRIPTOR_ALIASID_FIELD))
                                          .getValue()
                                          .split("\\r?\\n"));
                    ret = stream.map(String::trim)
                              .filter(it -> !StringUtils.isBlank(it))
                              .collect(Collectors.toSet());
                } catch (XWikiException e) {
                    this.logger.warn(String.format("Error while retrieving [%s]. Cause [{}].", documentReference), e);
                    ret = new HashSet<>();
                }
            }
        } catch (QueryException e) {
            this.logger.warn(String
                                 .format("Failed to query for XOjbects of type [%s.%s] with mainId = [%s]. Cause [{}].",
                                     SYSTEM_SPACE, UIXP_CLASS_NAME, uixpId), e);
            ret = null;
        }
        return ret;
    }
}
