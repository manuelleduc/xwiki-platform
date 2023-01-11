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
package org.xwiki.tag.internal;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;

import static org.xwiki.tag.internal.selector.ExhaustiveCheckTagsSelector.HINT;

/**
 * @version $Id$
 * @since x.y.z
 */
@Component(roles = { TagsSelectorProvider.class })
public class TagsSelectorProvider
{
    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configurationSource;

    @Inject
    @Named(HINT)
    private TagsSelector exhaustiveTagsSelector;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    /**
     * @return the tags selector component to use on this farm
     */
    public TagsSelector get()
    {
        String hint = this.configurationSource.getProperty("tag.selector.hint");
        if (hint == null) {
            return this.exhaustiveTagsSelector;
        }
        try {
            return this.componentManager.getInstance(TagsSelector.class, hint);
        } catch (ComponentLookupException e) {
            this.logger.error("Failed to get component [{}] with hint [{}]. Falling back to [{}].", TagsSelector.class,
                hint, HINT);
            return this.exhaustiveTagsSelector;
        }
    }
}
