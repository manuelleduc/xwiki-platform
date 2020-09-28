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
package com.xpn.xwiki.web;

import org.junit.jupiter.api.Test;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.csrf.CSRFToken;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.refactoring.RefactoringConfiguration;
import org.xwiki.resource.ResourceReferenceManager;
import org.xwiki.resource.entity.EntityResourceReference;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.mockito.MockitoComponentManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link DeleteAttachmentAction}.
 *
 * @version $Id$
 * @since 12.9RC1
 */
@ComponentTest
class DeleteAttachmentActionTest
{
    private DeleteAttachmentAction deleteAttachmentAction = new DeleteAttachmentAction();

    private CSRFToken csrfToken;

    private ResourceReferenceManager resourceReferenceManager;

    private ContextualLocalizationManager contextualLocalizationManager;

    private RefactoringConfiguration refactoringConfiguration;

    // Initialized by the setup() method.
    private XWikiRequest xWikiRequest;

    private XWikiDocument xWikiDocument;

    private XWikiAttachment xWikiAttachment;

    private DocumentAccessBridge documentAccessBridge;

    @BeforeComponent
    void setup(MockitoComponentManager componentManager) throws Exception
    {
        Utils.setComponentManager(componentManager);
        this.csrfToken = componentManager.registerMockComponent(CSRFToken.class);
        this.resourceReferenceManager = componentManager.registerMockComponent(ResourceReferenceManager.class);
        this.contextualLocalizationManager =
            componentManager.registerMockComponent(ContextualLocalizationManager.class);
        this.refactoringConfiguration = componentManager.registerMockComponent(RefactoringConfiguration.class);
        this.documentAccessBridge = componentManager.registerMockComponent(DocumentAccessBridge.class);
    }

    @Test
    void actionShouldntSkipRecycleBinAndRecycleBinSkippingDeactivated() throws Exception
    {
        XWikiContext xWikiContext = setup();
        when(this.xWikiRequest.getParameter("shouldSkipRecycleBin")).thenReturn(null);
        when(this.refactoringConfiguration.isRecycleBinSkippingActivated()).thenReturn(false);

        boolean actual = this.deleteAttachmentAction.action(xWikiContext);
        assertFalse(actual);

        verify(this.xWikiDocument).removeAttachment(this.xWikiAttachment, true);
    }

    @Test
    void actionShouldntSkipRecycleBinAndRecycleBinSkippingActivated() throws Exception
    {
        XWikiContext xWikiContext = setup();
        when(this.xWikiRequest.getParameter("shouldSkipRecycleBin")).thenReturn("false");
        when(this.refactoringConfiguration.isRecycleBinSkippingActivated()).thenReturn(true);

        boolean actual = this.deleteAttachmentAction.action(xWikiContext);
        assertFalse(actual);

        verify(this.xWikiDocument).removeAttachment(this.xWikiAttachment, true);
    }

    @Test
    void actionShouldSkipRecycleBinAndRecycleBinSkippingDeactivated() throws Exception
    {
        XWikiContext xWikiContext = setup();
        when(this.xWikiRequest.getParameter("shouldSkipRecycleBin")).thenReturn("true");
        when(this.refactoringConfiguration.isRecycleBinSkippingActivated()).thenReturn(false);

        boolean actual = this.deleteAttachmentAction.action(xWikiContext);
        assertFalse(actual);

        verify(this.xWikiDocument).removeAttachment(this.xWikiAttachment, true);
    }

    @Test
    void actionShouldSkipRecycleBinAndRecycleBinSkippingActivated() throws Exception
    {
        XWikiContext xWikiContext = setup();
        when(this.xWikiRequest.getParameter("shouldSkipRecycleBin")).thenReturn("true");
        when(this.refactoringConfiguration.isRecycleBinSkippingActivated()).thenReturn(true);
        when(this.documentAccessBridge.isAdvancedUser()).thenReturn(false);

        boolean actual = this.deleteAttachmentAction.action(xWikiContext);
        assertFalse(actual);

        verify(this.xWikiDocument).removeAttachment(this.xWikiAttachment, true);
    }

    @Test
    void actionShouldSkipRecycleBinAndRecycleBinSkippingActivatedAndAdvancedUser() throws Exception
    {
        XWikiContext xWikiContext = setup();
        when(this.xWikiRequest.getParameter("shouldSkipRecycleBin")).thenReturn("true");
        when(this.refactoringConfiguration.isRecycleBinSkippingActivated()).thenReturn(true);
        when(this.documentAccessBridge.isAdvancedUser()).thenReturn(true);

        boolean actual = this.deleteAttachmentAction.action(xWikiContext);
        assertFalse(actual);

        verify(this.xWikiDocument).removeAttachment(this.xWikiAttachment, false);
    }

    private XWikiContext setup()
    {
        XWikiContext xWikiContext = mock(XWikiContext.class);
        when(xWikiContext.get("ajax")).thenReturn(Boolean.TRUE);
        when(xWikiContext.getWiki()).thenReturn(mock(XWiki.class));
        this.xWikiDocument = mock(XWikiDocument.class);
        this.xWikiAttachment = mock(XWikiAttachment.class);
        when(this.xWikiAttachment.isImage(xWikiContext)).thenReturn(false);
        when(this.xWikiDocument.getAttachment("file.ext")).thenReturn(this.xWikiAttachment);
        when(xWikiContext.getDoc()).thenReturn(this.xWikiDocument);
        when(this.xWikiDocument.clone()).thenReturn(this.xWikiDocument);
        this.xWikiRequest = mock(XWikiRequest.class);
        when(xWikiContext.getRequest()).thenReturn(this.xWikiRequest);
        when(this.csrfToken.isTokenValid(any())).thenReturn(true);
        EntityResourceReference entityResourceReference = mock(EntityResourceReference.class);
        EntityReference documentReference = mock(EntityReference.class);
        when(documentReference.extractReference(EntityType.ATTACHMENT))
            .thenReturn(new DocumentReference("xwiki", "XWiki", "file.ext"));
        documentReference.extractReference(null);
        when(entityResourceReference.getEntityReference()).thenReturn(documentReference);
        when(this.resourceReferenceManager.getResourceReference()).thenReturn(entityResourceReference);
        return xWikiContext;
    }
}
