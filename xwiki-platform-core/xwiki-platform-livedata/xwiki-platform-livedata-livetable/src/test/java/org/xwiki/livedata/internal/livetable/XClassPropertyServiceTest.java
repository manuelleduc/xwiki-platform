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
package org.xwiki.livedata.internal.livetable;

import java.util.Optional;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.livedata.LiveDataException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.security.authorization.AccessDeniedException;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.PropertyInterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Test of {@link XClassPropertyService}.
 *
 * @version $Id$
 * @since 13.2RC1
 */
@ComponentTest
class XClassPropertyServiceTest
{
    @InjectMockComponents
    private XClassPropertyService xClassPropertyService;

    @MockComponent
    private ContextualAuthorizationManager authorization;

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @Mock
    private XWikiContext xcontext;

    @Mock
    private XWiki xwiki;

    @Mock
    private XWikiDocument document;

    @Mock
    private BaseObject baseObject;

    @Mock
    private PropertyInterface propertyInterface;

    @BeforeEach
    void setUp()
    {
        when(this.xcontextProvider.get()).thenReturn(this.xcontext);
        when(this.xcontext.getWiki()).thenReturn(this.xwiki);
    }

    @Test
    void updateWhenEditIsDisallowed() throws Exception
    {
        String property = "propertyName";
        Object value = "value";
        DocumentReference documentReference = new DocumentReference("xwiki", "MyApp", "mydoc");
        DocumentReference classReference = new DocumentReference("xwiki", "MyApp", "MyClass");

        doThrow(AccessDeniedException.class).when(this.authorization).checkAccess(Right.EDIT, documentReference);

        assertThrows(AccessDeniedException.class,
            () -> this.xClassPropertyService.update(property, value, documentReference, classReference));
        verifyNoInteractions(this.xcontextProvider);
    }

    @Test
    void updateXClassField() throws Exception
    {
        String property = "propertyName";
        Object value = "value";
        DocumentReference documentReference = new DocumentReference("xwiki", "MyApp", "mydoc");
        DocumentReference classReference = new DocumentReference("xwiki", "MyApp", "MyClass");

        when(this.xwiki.getDocument(documentReference, this.xcontext)).thenReturn(this.document);
        when(this.document.getXObject(classReference, 0)).thenReturn(this.baseObject);
        // The list of properties includes the property to update. 
        when(this.baseObject.getProperties()).thenReturn(new Object[] { "propertyA", "propertyName", "propertyB" });
        when(this.baseObject.get(property)).thenReturn(this.propertyInterface);
        when(this.propertyInterface.toFormString()).thenReturn("updatedValue");
        when(this.document.validate(this.xcontext)).thenReturn(true);
        when(this.document.isContentDirty()).thenReturn(true);

        Optional<Object> update = this.xClassPropertyService.update(property, value, documentReference, classReference);

        assertTrue(update.isPresent());
        assertEquals("updatedValue", update.get());

        verify(this.baseObject).set("propertyName", "value", this.xcontext);
        verify(this.xwiki).saveDocument(this.document, this.xcontext);
    }

    @Test
    void updateDocumentHiddenFieldButNotModified() throws Exception
    {
        String property = "doc.hidden";
        Object value = "true";
        DocumentReference documentReference = new DocumentReference("xwiki", "MyApp", "mydoc");

        when(this.xwiki.getDocument(documentReference, this.xcontext)).thenReturn(this.document);

        when(this.document.isContentDirty()).thenReturn(false);
        when(this.document.isHidden()).thenReturn(true);

        Optional<Object> update = this.xClassPropertyService.update(property, value, documentReference, null);
        assertTrue(update.isPresent());
        assertTrue((Boolean) update.get());

        verify(this.document).setHidden(true);
        verify(this.xwiki, never()).saveDocument(this.document, this.xcontext);
    }

    @Test
    void updateDocumentTitleFieldButNotValid() throws Exception
    {
        String property = "doc.title";
        Object value = "newTitle";
        DocumentReference documentReference = new DocumentReference("xwiki", "MyApp", "mydoc");

        when(this.xwiki.getDocument(documentReference, this.xcontext)).thenReturn(this.document);

        when(this.document.isContentDirty()).thenReturn(true);
        when(this.document.getTitle()).thenReturn("oldTitle");
        when(this.document.validate(this.xcontext)).thenReturn(false);

        LiveDataException liveDataException = assertThrows(LiveDataException.class,
            () -> this.xClassPropertyService.update(property, value, documentReference, null));
        assertEquals("Document not validated.", liveDataException.getMessage());

        verify(this.document).setTitle("newTitle");
        verify(this.xwiki, never()).saveDocument(this.document, this.xcontext);
    }

    @Test
    void updateDocumentContentField() throws Exception
    {
        String property = "doc.content";
        Object value = "newContent";
        DocumentReference documentReference = new DocumentReference("xwiki", "MyApp", "mydoc");

        when(this.xwiki.getDocument(documentReference, this.xcontext)).thenReturn(this.document);

        when(this.document.isContentDirty()).thenReturn(true);
        when(this.document.getContent()).thenReturn("oldContent");
        when(this.document.validate(this.xcontext)).thenReturn(true);

        Optional<Object> update = this.xClassPropertyService.update(property, value, documentReference, null);
        assertTrue(update.isPresent());
        assertEquals("oldContent", update.get());
        verify(this.document).setContent("newContent");
        verify(this.xwiki).saveDocument(this.document, this.xcontext);
    }
}