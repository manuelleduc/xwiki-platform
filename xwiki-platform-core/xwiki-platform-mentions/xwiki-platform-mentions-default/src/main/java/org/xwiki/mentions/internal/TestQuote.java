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
package org.xwiki.mentions.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.mentions.DisplayStyle;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Please document me.
 *
 * @version $Id$
 * @since X.Y.Z
 */
public class TestQuote
{
    public static void main(String[] args) throws ComponentLookupException
    {
        EmbeddableComponentManager classManager = new EmbeddableComponentManager();
        classManager.initialize(TestQuote.class.getClassLoader());
        QuoteService instance = classManager.getInstance(QuoteService.class);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("anchor", "anchor1");
        parameters.put("reference", "xwiki:XWiki.U1");
        parameters.put("style", DisplayStyle.FULL_NAME.name());
        XDOM mention = new XDOM(Arrays.asList(
            new ParagraphBlock(Arrays.asList(new RawBlock("hello world", Syntax.XHTML_1_0))),
            new MacroBlock("mention", parameters, true)));
        String extract = instance.extract(mention, "anchor1");
        System.out.println("____");
        System.out.println(extract);
        System.out.println("____");
    }
}
