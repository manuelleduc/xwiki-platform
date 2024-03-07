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
package org.xwiki.image.style;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.image.style.model.ImageStyle;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;

import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link ImageStyleScriptService}.
 *
 * @version $Id$
 * @since 14.3RC1
 */
@ComponentTest
class ImageStyleScriptServiceTest
{
    public static final String WIKI_ID = "wikiid";

    @InjectMockComponents
    private ImageStyleScriptService scriptService;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    @MockComponent
    private ImageStyleConfiguration imageStyleConfiguration;

    @MockComponent
    private ImageStyleManager imageStyleManager;

    @Mock
    private XWikiContext context;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @BeforeEach
    void setUp()
    {
        when(this.contextProvider.get()).thenReturn(this.context);
        when(this.context.getWikiId()).thenReturn(WIKI_ID);
    }

    @Test
    void getDefaultImageStyleRestPath()
    {
        assertEquals("/rest/wikis/wikiid/imageStyles/default", this.scriptService.getDefaultImageStyleRestPath());
    }

    @Test
    void getImageStylesRestPath()
    {
        assertEquals("/rest/wikis/wikiid/imageStyles", this.scriptService.getImageStylesRestPath());
    }

    @Test
    void getDefaultImageStyleConfigurationNoDefault() throws Exception
    {
        String documentReference = "Space.Page";
        when(this.imageStyleConfiguration.getDefaultStyle(WIKI_ID, documentReference)).thenReturn(null);
        assertEquals(Map.of(), this.scriptService.getDefaultImageStyleConfiguration(documentReference));
    }

    @Test
    void getDefaultImageStyleConfigurationWithException() throws Exception
    {
        String documentReference = "Space.Page";
        when(this.imageStyleConfiguration.getDefaultStyle(WIKI_ID, documentReference))
            .thenThrow(ImageStyleException.class);
        assertEquals(Map.of(), this.scriptService.getDefaultImageStyleConfiguration(documentReference));
        assertEquals("Failed to access default image style configuration for [Space.Page] on wiki [wikiid]. "
            + "Cause: [ImageStyleException: ]", this.logCapture.getMessage(0));
        assertEquals(Level.WARN, this.logCapture.getLogEvent(0).getLevel());
    }

    @Test
    void getDefaultImageStyleConfigurationWrongDefaultStyle() throws Exception
    {
        String documentReference = "Space.Page";
        when(this.imageStyleConfiguration.getDefaultStyle(WIKI_ID, documentReference)).thenReturn("s1");
        ImageStyle s1 = new ImageStyle();
        s1.setIdentifier("s2");
        s1.setType("stl2");
        when(this.imageStyleManager.getImageStyles(WIKI_ID)).thenReturn(Set.of(s1));
        assertEquals(Map.of(), this.scriptService.getDefaultImageStyleConfiguration(documentReference));
        verify(this.imageStyleConfiguration, never()).getForceDefaultStyle(anyString(), anyString());
    }

    @Test
    void getDefaultImageStyleConfigurationNotForced() throws Exception
    {
        String documentReference = "Space.Page";
        when(this.imageStyleConfiguration.getDefaultStyle(WIKI_ID, documentReference)).thenReturn("s1");
        ImageStyle s1 = new ImageStyle();
        s1.setIdentifier("s1");
        s1.setType("stl1");
        when(this.imageStyleManager.getImageStyles(WIKI_ID)).thenReturn(Set.of(s1));
        when(this.imageStyleConfiguration.getForceDefaultStyle(WIKI_ID, documentReference)).thenReturn(false);
        Map<String, Object> expected = new java.util.HashMap<>();
        expected.put("defaultHeight", null);
        expected.put("defaultAlignment", null);
        expected.put("type", "stl1");
        expected.put("defaultBorder", null);
        expected.put("defaultWidth", null);
        expected.put("defaultTextWrap", null);
        assertEquals(expected, this.scriptService.getDefaultImageStyleConfiguration(documentReference));
    }

    @Test
    void getDefaultImageStyleConfigurationForced() throws Exception
    {
        String documentReference = "Space.Page";
        when(this.imageStyleConfiguration.getDefaultStyle(WIKI_ID, documentReference)).thenReturn("s1");
        ImageStyle s1 = new ImageStyle();
        s1.setIdentifier("s1");
        s1.setType("stl1");
        when(this.imageStyleManager.getImageStyles(WIKI_ID)).thenReturn(Set.of(s1));
        when(this.imageStyleConfiguration.getForceDefaultStyle(WIKI_ID, documentReference)).thenReturn(true);
        Map<String, Object> expected = new java.util.HashMap<>();
        expected.put("defaultHeight", null);
        expected.put("defaultAlignment", null);
        expected.put("defaultBorder", null);
        expected.put("defaultWidth", null);
        expected.put("defaultTextWrap", null);
        assertEquals(expected, this.scriptService.getDefaultImageStyleConfiguration(documentReference));
    }

}
