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

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

import static com.xpn.xwiki.objects.classes.TextAreaClass.ContentType.VELOCITY_CODE;

/**
 * Define the NotificationQuickActionClass XObjects.
 *
 * @version $Id$
 * @since 12.9RC1
 */
@Component
@Named(WikiNotificationQuickActionDocumentInitializer.XCLASS_NAME)
@Singleton
public class WikiNotificationQuickActionDocumentInitializer extends AbstractMandatoryClassInitializer
{
    /**
     * The name of the XObject class that should be bound used.
     */
    public static final String XCLASS_NAME = "XWiki.Notifications.Code.NotificationQuickActionClass";

    /**
     * The name of the event type property in the XObject.
     */
    public static final String EVENT_TYPE = "eventType";

    /**
     * The template to render the quick action.
     */
    public static final String QUICK_ACTION_TEMPLATE = "quickActionTemplate";

    /**
     * The name of the space where the class is located.
     */
    private static final List<String> SPACE_PATH = Arrays.asList("XWiki", "Notifications", "Code");

    /**
     * Reference of the XClass to create.
     */
    public static final LocalDocumentReference CLASS_REFERENCE
        = new LocalDocumentReference(SPACE_PATH, "NotificationQuickActionClass");

    /**
     * Default constructor.
     */
    public WikiNotificationQuickActionDocumentInitializer()
    {
        super(CLASS_REFERENCE);
    }

    @Override
    protected void createClass(BaseClass xclass)
    {
        xclass.addTextField(EVENT_TYPE, "Event type", 64);
        xclass.addTextAreaField(QUICK_ACTION_TEMPLATE, "Quick Action template", 40, 3, VELOCITY_CODE);
    }
}
