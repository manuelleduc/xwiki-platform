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
package org.xwiki.mentions.internal.listeners;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.event.ExtensionUpgradedEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.xar.XarEntryType;

/**
 * Please document me.
 *
 * @version $Id$
 * @since 12.8RC1
 */
@Component
@Named(MentionsUpgrade128.LISTENER_NAME)
public class MentionsUpgrade128 implements EventListener
{
    /**
     * The name of the listener.
     */
    public static final String LISTENER_NAME = "MentionsUpgrade128";

    /**
     * The ID of the extension for which the listener should react.
     */
    private static final String EXTENSION_ID = "org.xwiki.platform:xwiki-platform-mentions-ui";

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    @Override
    public String getName()
    {
        return LISTENER_NAME;
    }

    @Override
    public List<Event> getEvents()
    {
        return Collections.singletonList(new ExtensionUpgradedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        this.logger.error("#####MentionsUpgrade128#####MentionsUpgrade128#####MentionsUpgrade128");
        if (event instanceof ExtensionUpgradedEvent) {
            // Install and upgrade events provide the same source
            InstalledExtension installedExtension = (InstalledExtension) source;

            if (Objects.equals(EXTENSION_ID, installedExtension.getId().getId())) {
                registerXarEntryTypes();
            }
        }
    }

    private void registerXarEntryTypes()
    {
        for (XarEntryType.UpgradeType upgradeType : XarEntryType.UpgradeType.values()) {
            // // Register a XAR entry type for every possible scenario
            // createAndRegisterEntryType(upgradeType, true, true);
            // createAndRegisterEntryType(upgradeType, true, false);
            // createAndRegisterEntryType(upgradeType, false, true);
            // createAndRegisterEntryType(upgradeType, false, false);

        }
    }

    // private void createAndRegisterEntryType(XarEntryType.UpgradeType upgradeType, boolean isEditAllowed, boolean isDeleteAllowed)
    // {
    //     CustomizableXarEntryType customizableXarEntryType =
    //         new CustomizableXarEntryType(upgradeType, isEditAllowed, isDeleteAllowed);
    //
    //     DefaultComponentDescriptor<XarEntryType> entryTypeDescriptor = new DefaultComponentDescriptor<>();
    //     entryTypeDescriptor.setImplementation(CustomizableXarEntryType.class);
    //     entryTypeDescriptor.setRoleType(XarEntryType.class);
    //     entryTypeDescriptor.setRoleHint(customizableXarEntryType.getName());
    //
    //     logger.debug("Registering XAR entry type [{}].", customizableXarEntryType.getName());
    //     try {
    //         if (componentManager.hasComponent(XarEntryType.class, customizableXarEntryType.getName())) {
    //             logger.debug("Removing previously registered entry type [{}]", customizableXarEntryType.getName());
    //             componentManager.unregisterComponent(XarEntryType.class, customizableXarEntryType.getName());
    //         }
    //
    //         componentManager.registerComponent(entryTypeDescriptor, customizableXarEntryType);
    //     } catch (ComponentRepositoryException e) {
    //         logger.error("Failed to register XAR entry type [{}]. The installation of extensions using this type may "
    //                          + "fail !", customizableXarEntryType.getName());
    //     }
    // }
}
