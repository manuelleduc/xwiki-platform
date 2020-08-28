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
package org.xwiki.uiextension.script;

import java.util.Set;

/**
 * Hold the data of a rows of the UIXP livetable.
 *
 * @version $Id$
 * @since 12.8RC1
 */
public class UIXPDescriptor
{
    private String mainId;

    private Set<String> aliases;

    /**
     *
     * @param mainId The main UIXP id
     * @return The current object
     */
    public UIXPDescriptor setMainId(String mainId)
    {
        this.mainId = mainId;
        return this;
    }

    /**
     *
     * @return The main UIXP id
     */
    public String getMainId()
    {
        return this.mainId;
    }

    /**
     *
     * @param aliases The UIXP aliases
     * @return The current object
     */
    public UIXPDescriptor setAliases(Set<String> aliases)
    {
        this.aliases = aliases;
        return this;
    }

    /**
     *
     * @return The UIXP aliases
     */
    public Set<String> getAliases()
    {
        return this.aliases;
    }
}
