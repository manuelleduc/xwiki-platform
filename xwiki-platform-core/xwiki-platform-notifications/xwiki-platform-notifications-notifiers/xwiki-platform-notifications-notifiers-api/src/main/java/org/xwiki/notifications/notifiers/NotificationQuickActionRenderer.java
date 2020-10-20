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
package org.xwiki.notifications.notifiers;

import org.xwiki.component.annotation.Role;
import org.xwiki.eventstream.Event;
import org.xwiki.notifications.NotificationException;
import org.xwiki.rendering.block.Block;

/**
 * Render the quick actions of an event.
 *
 * @version $Id$
 * @since 12.9RC1
 */
@Role
public interface NotificationQuickActionRenderer
{
    /**
     * Generate a rendering {@link Block} for a given event to display its quick actions.
     * @param event the event to render
     * @return a rendering block ready to display the event's quick actions
     * @throws NotificationException if an error happens
     */
    Block render(Event event) throws NotificationException;
}
