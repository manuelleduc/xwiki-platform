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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.component.wiki.internal.bridge.WikiBaseObjectComponentBuilder;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.notifications.NotificationException;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.template.TemplateManager;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static java.util.Collections.singletonList;
import static org.xwiki.notifications.notifiers.internal.WikiNotificationQuickActionDocumentInitializer.CLASS_REFERENCE;

/**
 * This component allows the definition of a {@link WikiQuickActionDisplayer} in wiki pages. It uses {@link
 * org.xwiki.eventstream.UntypedRecordableEvent#getEventType} to be bound to a specific event type.
 *
 * @version $Id$
 * @since 12.9RC1
 */
@Component
@Named(WikiNotificationQuickActionDocumentInitializer.XCLASS_NAME)
@Singleton
public class WikiNotificationQuickActionComponentBuilder implements WikiBaseObjectComponentBuilder
{
    @Inject
    private TemplateManager templateManager;

    @Inject
    private ScriptContextManager scriptContextManager;

    @Inject
    private AuthorizationManager authorizationManager;

    @Override
    public List<WikiComponent> buildComponents(BaseObject baseObject) throws WikiComponentException
    {
        try {
            // Check that the document owner is allowed to build the components
            XWikiDocument parentDocument = baseObject.getOwnerDocument();
            this.checkRights(parentDocument.getDocumentReference(), parentDocument.getAuthorReference());

            // Instantiate the component
            return singletonList(instantiateComponent(baseObject, baseObject.getOwnerDocument().getAuthorReference(),
                this.scriptContextManager,
                this.templateManager));
        } catch (Exception e) {
            throw new WikiComponentException(String.format(
                "Unable to build the WikiNotificationDisplayer wiki component "
                    + "for [%s].", baseObject), e);
        }
    }

    private void checkRights(DocumentReference documentReference, DocumentReference authorReference)
        throws NotificationException
    {
        if (!this.authorizationManager.hasAccess(Right.ADMIN, authorReference, documentReference.getWikiReference())) {
            throw new NotificationException(
                "Registering custom Notification Quick Action Displayers requires wiki administration rights.");
        }
    }

    private WikiComponent instantiateComponent(BaseObject baseObject,
        DocumentReference authorReference, ScriptContextManager scriptContextManager,
        TemplateManager templateManager) throws NotificationException
    {
        return new WikiQuickActionDisplayer(baseObject, authorReference, scriptContextManager, templateManager);
    }

    @Override
    public EntityReference getClassReference()
    {
        return CLASS_REFERENCE;
    }
}
