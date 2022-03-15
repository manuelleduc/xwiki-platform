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
package org.xwiki.image.style.internal;

import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.image.style.ImageStyleException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link DefaultImageStyleManager}.
 *
 * @version $Id$
 * @since 14.2RC1
 */
@ComponentTest
class DefaultImageStyleManagerTest
{
    private static final DocumentReference DOCUMENT_REFERENCE_DOC1 = new DocumentReference("wiki", "space", "doc1");

    private static final DocumentReference DOCUMENT_REFERENCE_DOC2 = new DocumentReference("wiki", "space", "doc2");

    @InjectMockComponents
    private DefaultImageStyleManager manager;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @MockComponent
    private ExecutionContextManager contextManager;

    @MockComponent
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Mock
    private XWikiContext context;

    @Mock
    private Query query;

    @Mock
    private XWiki wiki;

    @Mock
    private XWikiDocument doc1;

    @Mock
    private XWikiDocument doc2;

    @BeforeEach
    void setUp() throws Exception
    {
        when(this.xcontextProvider.get()).thenReturn(this.context);
        when(this.queryManager.createQuery("select doc.fullName "
                + "from Document doc, doc.object(Image.Style.Code.ImageStyleClass) as obj "
                + "where doc.space = 'Image.Style.Code.ImageStyles'",
            Query.XWQL)).thenReturn(this.query);
        when(this.query.setWiki(anyString())).thenReturn(this.query);
        when(this.documentReferenceResolver.resolve("doc1")).thenReturn(DOCUMENT_REFERENCE_DOC1);
        when(this.documentReferenceResolver.resolve("doc2")).thenReturn(DOCUMENT_REFERENCE_DOC2);
        when(this.context.getWiki()).thenReturn(this.wiki);
        when(this.wiki.getDocument(DOCUMENT_REFERENCE_DOC1, this.context)).thenReturn(this.doc1);
        when(this.wiki.getDocument(DOCUMENT_REFERENCE_DOC2, this.context)).thenReturn(this.doc2);
    }

    @Test
    void getImageStyles() throws Exception
    {
        when(this.query.execute()).thenReturn(List.of("doc1", "docfail", "doc2"));
        assertEquals(Set.of(), this.manager.getImageStyles("wiki"));
        verify(this.contextManager).pushContext(any(ExecutionContext.class), eq(false));
        verify(this.contextManager).popContext();
        verify(this.context).setWiki("wiki");
        verify(this.query).setWiki("wiki");
    }

    @Test
    void getImageStylesQueryException() throws Exception
    {
        when(this.query.execute()).thenThrow(QueryException.class);
        ImageStyleException exception =
            assertThrows(ImageStyleException.class, () -> this.manager.getImageStyles("wiki"));
        assertEquals("Failed to initialize the execution context", exception.getMessage());
        assertEquals(QueryException.class, exception.getCause().getClass());
        verify(this.contextManager).pushContext(any(ExecutionContext.class), eq(false));
        verify(this.contextManager).popContext();
        verify(this.context).setWiki("wiki");
        verify(this.query).setWiki("wiki");
    }

    @Test
    void getImageStylesContextException() throws Exception
    {
        doThrow(ExecutionContextException.class).when(this.contextManager)
            .pushContext(any(ExecutionContext.class), anyBoolean());
        ImageStyleException exception =
            assertThrows(ImageStyleException.class, () -> this.manager.getImageStyles("wiki"));
        assertEquals("Failed to initialize the execution context", exception.getMessage());
        assertEquals(ExecutionContextException.class, exception.getCause().getClass());
        verify(this.contextManager).pushContext(any(ExecutionContext.class), eq(false));
        verify(this.contextManager).popContext();
        verify(this.context).setWiki("wiki");
    }
}
