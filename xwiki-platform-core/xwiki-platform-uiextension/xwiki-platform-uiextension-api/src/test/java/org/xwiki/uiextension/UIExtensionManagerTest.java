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
package org.xwiki.uiextension;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.uiextension.internal.DefaultUIExtensionManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.LargeStringProperty;

import static com.xpn.xwiki.XWiki.SYSTEM_SPACE;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xwiki.query.Query.XWQL;

/**
 * Test of {@link DefaultUIExtensionManager}.
 *
 * @version $Id$
 * @since 12.7RC1
 */
@ComponentTest
public class UIExtensionManagerTest
{
    @InjectMockComponents
    private DefaultUIExtensionManager uiExtensionManager;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @MockComponent
    private Provider<XWikiContext> xcontextProvider;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Test
    void get() throws Exception
    {
        Query query = mock(Query.class);
        XWikiContext context = mock(XWikiContext.class);
        XWiki xWiki = mock(XWiki.class);
        XWikiDocument document = mock(XWikiDocument.class);
        ComponentManager componentManager = mock(ComponentManager.class);
        UIExtension uix1 = mock(UIExtension.class);
        UIExtension uix2 = mock(UIExtension.class);

        when(this.queryManager.createQuery(
            "from doc.object(XWiki.UIExtensionPointDescriptorClass) as uixp "
                + "where uixp.mainId = :mainId", XWQL))
            .thenReturn(query);
        when(query.execute()).thenReturn(singletonList("xwiki:XWiki.Doc"));
        when(this.xcontextProvider.get()).thenReturn(context);
        when(context.getWiki()).thenReturn(xWiki);
        when(context.getWikiId()).thenReturn("xwiki");

        DocumentReference documentReference = new DocumentReference("xwiki", "XWiki", "Doc");
        when(this.documentReferenceResolver.resolve("xwiki:XWiki.Doc"))
            .thenReturn(documentReference);
        when(xWiki.getDocument(documentReference, context)).thenReturn(document);
        BaseObject baseObject = new BaseObject();
        LargeStringProperty element = new LargeStringProperty();
        element.setValue("alias.b.c");
        baseObject.addField("aliasIds", element);
        when(document.getXObjects(new DocumentReference("xwiki", SYSTEM_SPACE, "UIExtensionPointDescriptorClass")))
            .thenReturn(singletonList(baseObject));

        when(this.contextComponentManagerProvider.get()).thenReturn(componentManager);
        when(componentManager.getInstanceList(UIExtension.class)).thenReturn(Arrays.asList(uix1, uix2));
        when(uix1.getExtensionPointId()).thenReturn("extensionpoint");
        when(uix2.getExtensionPointId()).thenReturn("alias.b.c");
        
        List<UIExtension> actual = this.uiExtensionManager.get("extensionpoint");

        List<UIExtension> expected = Arrays.asList(uix1, uix2);
        assertEquals(expected, actual);
    }
}
