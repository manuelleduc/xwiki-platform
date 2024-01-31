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
package org.xwiki.notifications.filters.internal.livedata;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.livedata.LiveData;
import org.xwiki.livedata.LiveDataEntryStore;
import org.xwiki.livedata.LiveDataException;
import org.xwiki.livedata.LiveDataQuery;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.notifications.NotificationException;
import org.xwiki.notifications.filters.NotificationFilter;
import org.xwiki.notifications.filters.NotificationFilterManager;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

import com.xpn.xwiki.XWikiContext;

import static org.xwiki.notifications.filters.internal.livedata.NotificationFilterPreferenceLiveDataSource.NOTIFICATION_FILTER_PREFERENCE;

/**
 * @version $Id$
 * @since 16.1.0RC1
 */
@Component
@Named(NOTIFICATION_FILTER_PREFERENCE)
@Singleton
public class NotificationFilterPreferenceEntryStore implements LiveDataEntryStore
{
    private static final String USER_SOURCE_PARAM = "user";

    @Inject
    private DocumentReferenceResolver<String> entityReferenceResolver;

    @Inject
    private ContextualAuthorizationManager contextualAuthorizationManager;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Inject
    private NotificationFilterManager notificationFilterManager;

    @Override
    public Optional<Map<String, Object>> get(Object entryId) throws LiveDataException
    {
        return Optional.empty();
    }

    @Override
    public LiveData get(LiveDataQuery query) throws LiveDataException
    {
        // TODO port the select logic here
        Map<String, Object> sourceParameters = query.getSource().getParameters();

        boolean isAccessGranted = true;
        DocumentReference requestedUserDocRef = null;
        XWikiContext xWikiContext = this.xWikiContextProvider.get();
        WikiReference wikiReference = null;
        if (sourceParameters.containsKey(USER_SOURCE_PARAM)) {
            String user = (String) sourceParameters.get(USER_SOURCE_PARAM);
            requestedUserDocRef = this.entityReferenceResolver.resolve(user);
            isAccessGranted = this.contextualAuthorizationManager.hasAccess(Right.ADMIN, requestedUserDocRef)
                || xWikiContext.getUserReference().equals(requestedUserDocRef);
        } else {
            wikiReference = xWikiContext.getWikiReference();
        }

        if (!isAccessGranted) {
            // TODO: improve error message.
            throw new LiveDataException("Access not allowed");
        }

        Object type = sourceParameters.getOrDefault("type", "");
        boolean displaySystem = true;
        boolean displayCustom = true;
        if (type.equals("custom")) {
            displaySystem = false;
        } else if (type.equals("system")) {
            displayCustom = false;
        }

        if (displaySystem) {
            Set<NotificationFilter> systemFilters = getSystemFilters(requestedUserDocRef, wikiReference);
            
        }

        if (displayCustom) {

        }

        return new LiveData();
    }

    private Set<NotificationFilter> getSystemFilters(DocumentReference requestedUserDocRef, WikiReference wikiReference)
        throws LiveDataException
    {
        try {
            Set<NotificationFilter> filters;
            if (requestedUserDocRef != null) {
                filters = this.notificationFilterManager.getToggleableFilters(
                        this.notificationFilterManager.getAllFilters(requestedUserDocRef, false))
                    .collect(Collectors.toSet());
            } else {
                filters = this.notificationFilterManager.getToggleableFilters(
                    this.notificationFilterManager.getAllFilters(wikiReference)).collect(
                    Collectors.toSet());
            }
            return filters;
        } catch (NotificationException e) {
            throw new LiveDataException("Failed to get system filters", e);
        }
    }
}
