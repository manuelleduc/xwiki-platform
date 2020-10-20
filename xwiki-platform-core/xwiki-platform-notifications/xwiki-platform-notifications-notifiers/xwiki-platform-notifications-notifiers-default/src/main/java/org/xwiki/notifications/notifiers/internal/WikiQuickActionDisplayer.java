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

import java.lang.reflect.Type;
import java.util.List;

import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentScope;
import org.xwiki.eventstream.Event;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.notifications.NotificationException;
import org.xwiki.notifications.notifiers.NotificationQuickActionDisplayer;
import org.xwiki.rendering.block.Block;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.template.Template;
import org.xwiki.template.TemplateManager;

import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseObjectReference;

import static java.util.Collections.singletonList;
import static javax.script.ScriptContext.ENGINE_SCOPE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.xwiki.notifications.notifiers.internal.WikiNotificationQuickActionDocumentInitializer.EVENT_TYPE;

/**
 * This class is meant to be instanciated and then registered to the Component Manager by the {@link
 * WikiNotificationDisplayerComponentBuilder} component every time a document containing a NotificationDisplayerClass is
 * added, updated or deleted.
 *
 * @version $Id$
 * @since 12.9RC1
 */
public class WikiQuickActionDisplayer implements NotificationQuickActionDisplayer, WikiComponent
{
    private static final String EVENT_BINDING_NAME = "event";

    private final BaseObjectReference objectReference;

    private final DocumentReference authorReference;

    private final String eventType;

    private final List<String> supportedEvents;

    private final ScriptContextManager scriptContextManager;

    private final TemplateManager templateManager;



    private Template template;

    /**
     * @param baseObject the XObject which has the required properties to instantiate the component
     * @param authorReference the author reference of the document
     * @param scriptContextManager the {@link ScriptContextManager} to use
     * @param templateManager the {@link TemplateManager} to use
     * @throws NotificationException in case of error during the initialization of the object
     */
    public WikiQuickActionDisplayer(BaseObject baseObject, DocumentReference authorReference,
        ScriptContextManager scriptContextManager, TemplateManager templateManager) throws NotificationException
    {
        this.objectReference = baseObject.getReference();
        this.authorReference = authorReference;
        String supportedEvent = baseObject.getStringValue(EVENT_TYPE);
        this.eventType = supportedEvent;
        this.supportedEvents = singletonList(supportedEvent);
        this.scriptContextManager = scriptContextManager;
        this.templateManager = templateManager;

        String xObjectTemplate =
            baseObject.getStringValue(WikiNotificationQuickActionDocumentInitializer.QUICK_ACTION_TEMPLATE);
        if (isNotBlank(xObjectTemplate)) {
            try {
                this.template = templateManager.createStringTemplate(xObjectTemplate, authorReference);
            } catch (Exception e) {
                throw new NotificationException(String
                    .format("Unable to initialize the quick action notification template for the [%s].",
                        this.eventType), e);
            }
        }
    }

    @Override
    public DocumentReference getDocumentReference()
    {
        return (DocumentReference) this.objectReference.getParent();
    }

    @Override
    public DocumentReference getAuthorReference()
    {
        return this.authorReference;
    }

    @Override
    public Type getRoleType()
    {
        return NotificationQuickActionDisplayer.class;
    }

    @Override
    public String getRoleHint()
    {
        return this.eventType;
    }

    @Override
    public WikiComponentScope getScope()
    {
        return WikiComponentScope.WIKI;
    }

    @Override
    public Block render(Event event) throws NotificationException
    {
        if (this.template != null) {
            // Save the old value in the context that refers to EVENT_BINDING_NAME
            Object oldContextAttribute =
                this.scriptContextManager.getCurrentScriptContext().getAttribute(EVENT_BINDING_NAME, ENGINE_SCOPE);

            try {
                // Allow the template to access the event during its execution
                this.scriptContextManager.getCurrentScriptContext()
                    .setAttribute(EVENT_BINDING_NAME, event, ENGINE_SCOPE);

                return this.templateManager.execute(this.template);
            } catch (Exception e) {
                throw new NotificationException(
                    String.format("Unable to render notification template for the [%s].", this.eventType), e);
            } finally {
                // Restore the old object associated with EVENT_BINDING_NAME
                this.scriptContextManager.getCurrentScriptContext()
                    .setAttribute(EVENT_BINDING_NAME, oldContextAttribute, ENGINE_SCOPE);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<String> getSupportedEvents()
    {
        return this.supportedEvents;
    }
}
