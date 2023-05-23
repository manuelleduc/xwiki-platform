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
package org.xwiki.requiredrights.internal;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.index.TaskManager;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.observation.remote.RemoteObservationManagerContext;

import com.xpn.xwiki.doc.XWikiDocument;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Listener for created documents and analyze the required rights they need.
 *
 * @version $Id$
 * @since 15.5RC1
 */
@Component
@Singleton
@Named(RequiredRightsDocumentListener.ID)
public class RequiredRightsDocumentListener extends AbstractEventListener
{
    /**
     * The unique id of this listener. Used to uniquely identify this listener, and also as its component hint.
     */
    public static final String ID = "RequiredRightsDocumentListener";

    @Inject
    private RemoteObservationManagerContext remoteObservationManagerContext;

    @Inject
    private TaskManager taskManager;

    @Inject
    private AnalysisResultsService analysisResultsService;

    @Inject
    private Logger logger;

    /**
     * Default constructor.
     */
    public RequiredRightsDocumentListener()
    {
        super(ID, List.of(new DocumentCreatedEvent(), new DocumentUpdatedEvent(), new DocumentDeletedEvent()));
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument doc = (XWikiDocument) source;
        // Only handle locale event, and only on document where the required rights are not already activated.
        if (!this.remoteObservationManagerContext.isRemoteState()
            && Objects.equals(FALSE, doc.isRequiredRightsActivated()))
        {
            if (event instanceof DocumentCreatedEvent) {
                queue(doc);
            } else if (event instanceof DocumentUpdatedEvent) {
                // TODO: don't forget to show the result as outdated if the analysis was not done on the more recent
                //  version.
                queue(doc);
            } else if (event instanceof DocumentDeletedEvent) {
                try {
                    this.analysisResultsService.removeResult(doc);
                } catch (RequiredRightException e) {
                    this.logger.warn("Failed to remove results for deleted doc [{}]. Cause: [{}]",
                        doc.getDocumentReference(), getRootCauseMessage(e));
                }
            } else {
                this.logger.warn("Event [{}] was not expected.", event);
            }
        }
    }

    private void queue(XWikiDocument doc)
    {
        this.taskManager.addTask(doc.getDocumentReference().getWikiReference().getName(),
            doc.getId(), DefaultRequiredRightsAnalyzerTaskConsumer.ID);
    }
}
