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
package org.xwiki.mentions.internal.listeners;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.mentions.MentionsEventExecutor;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.LargeStringProperty;

import ch.qos.logback.classic.Level;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.mentions.MentionLocation.AWM_FIELD;
import static org.xwiki.mentions.MentionLocation.DOCUMENT;
import static org.xwiki.test.LogLevel.DEBUG;

/**
 * Test of {@link MentionsCreatedEventListener}.
 *
 * @version $Id$
 * @since 12.5RC1
 */
@ComponentTest
public class MentionsCreatedEventListenerTest
{
    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(DEBUG);

    @InjectMockComponents
    private MentionsCreatedEventListener listener;

    @Mock
    private XWikiDocument document;

    @MockComponent
    private MentionsEventExecutor executor;

    @Test
    void onEvent()
    {

        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Doc");
        DocumentReference authorReference = new DocumentReference("xwiki", "XWiki", "Author");
        DocumentCreatedEvent event = new DocumentCreatedEvent(documentReference);
        XDOM xdom = new XDOM(emptyList());

        when(this.document.getDocumentReference()).thenReturn(documentReference);
        when(this.document.getAuthorReference()).thenReturn(authorReference);
        when(this.document.getXDOM()).thenReturn(xdom);
        Map<DocumentReference, List<BaseObject>> xObjects = new HashMap<>();
        BaseObject baseObject = new BaseObject();
        LargeStringProperty element = new LargeStringProperty();
        element.setValue("LSP CONTENT");
        baseObject.addField("lsp", element);
        xObjects.put(documentReference, singletonList(baseObject));
        when(this.document.getXObjects()).thenReturn(xObjects);

        this.listener.onEvent(event, this.document, null);

        assertEquals(1, this.logCapture.size());
        assertEquals(Level.DEBUG, this.logCapture.getLogEvent(0).getLevel());
        assertEquals("Event [org.xwiki.bridge.event.DocumentCreatedEvent] received from [document] with data [null].",
            this.logCapture.getMessage(0));

        verify(this.executor).executeCreate(xdom, authorReference, documentReference, DOCUMENT);
        verify(this.executor).executeCreate("LSP CONTENT", authorReference, documentReference, AWM_FIELD);
    }
}