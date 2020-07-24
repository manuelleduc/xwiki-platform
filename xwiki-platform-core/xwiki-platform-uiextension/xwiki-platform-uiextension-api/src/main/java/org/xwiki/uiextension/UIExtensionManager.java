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
package org.xwiki.uiextension;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.uiextension.script.UIXPDescriptor;

/**
 * A UIExtensionManager retrieves extensions for a given extension point.
 *
 * @version $Id$
 * @since 4.3.1
 */
@Role
public interface UIExtensionManager
{
    /**
     * Retrieves all the {@link UIExtension}s for a given Extension Point.
     *
     * @param extensionPointId The ID of the Extension Point to retrieve the {@link UIExtension}s for
     * @return the list of {@link UIExtension} for the given Extension Point
     */
    List<UIExtension> get(String extensionPointId);

    /**
     *
     * @return a list of UIXP descriptors.
     * @param offset
     * @param limit
     * @param idFilter
     * @param filter
     * @param sort
     * @param dir
     */
    List<UIXPDescriptor> getUIXPDescriptors(Long offset, Long limit, String idFilter, String filter,
        String sort, String dir);

    /**
     *
     * @return the total numver of UIXP descriptors.
     * @param mainIdFilter
     * @param aliasesFilter
     */
    long getUIXPDescriptorsTotal(String mainIdFilter, String aliasesFilter);
}
