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
package org.xwiki.notifications.notifiers.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.namespace.NamespaceContextExecutor;
import org.xwiki.eventstream.Event;
import org.xwiki.model.namespace.WikiNamespace;
import org.xwiki.notifications.NotificationException;
import org.xwiki.notifications.notifiers.NotificationQuickActionDisplayer;
import org.xwiki.notifications.notifiers.NotificationQuickActionRenderer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ParagraphBlock;

/**
 * Default implementation for {@link NotificationQuickActionRenderer}.
 *
 * @version $Id$
 * @since 12.9
 */
@Component
@Singleton
public class DefaultNotificationQuickActionRenderer implements NotificationQuickActionRenderer
{
    @Inject
    @Named("context")
    private ComponentManager componentManager;

    @Inject
    private NamespaceContextExecutor namespaceContextExecutor;

    @Override
    public Block render(Event event) throws NotificationException
    {
        try {
            if (event.getDocument() != null) {
                return this.namespaceContextExecutor.execute(
                    new WikiNamespace(event.getDocument().getWikiReference().getName()),
                    () -> renderCompositeEvent(event));
            } else {
                return renderCompositeEvent(event);
            }
        } catch (Exception e) {
            throw new NotificationException("Failed to render the notification.", e);
        }
    }

    private Block renderCompositeEvent(Event event)
        throws ComponentLookupException, NotificationException
    {
        List<Block> rendered = new ArrayList<>();
        for (NotificationQuickActionDisplayer displayer : lookupDisplayers(event)) {
            rendered.add(displayer.render(event));
        }

        return new ParagraphBlock(rendered);
    }

    private List<NotificationQuickActionDisplayer> lookupDisplayers(Event event)
        throws ComponentLookupException
    {
        List<NotificationQuickActionDisplayer> ret = new ArrayList<>();
        // Lookup all displayers
        for (NotificationQuickActionDisplayer displayer
            : this.componentManager.<NotificationQuickActionDisplayer>getInstanceList(
            NotificationQuickActionDisplayer.class)) {

            // Return the displayer if it supports the given event
            for (String supportedEvent : displayer.getSupportedEvents()) {
                if (Objects.equals(supportedEvent, event.getType())) {
                    ret.add(displayer);
                }
            }
        }

        return ret;
    }
}
