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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.BlockMatcher;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.transformation.Transformation;

import com.xpn.xwiki.XWikiContext;

import static org.xwiki.rendering.block.Block.Axes.DESCENDANT;

/**
 * Default implementation of {@link QuoteService}.
 *
 * @version $Id$
 * @since 12.6RC1
 */
@Component
@Singleton
public class DefaultQuoteService implements QuoteService
{
    private static final Class<?>[] WHITE_LIST_BLOCK = new Class<?>[]{
        WordBlock.class,
        SpaceBlock.class,
        SpecialSymbolBlock.class,
        FormatBlock.class
    };

    @Inject
    @Named("plain/1.0")
    private BlockRenderer renderer;

    @Inject
    @Named("macro")
    private Transformation transformation;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override
    public Optional<String> extract(XDOM xdom, String anchorId)
    {

        //  WordBlock, SpaceBlock, SpecialCharacterBlock FormatBlock

        return Optional.ofNullable(xdom.getFirstBlock(findMatchingMention(anchorId), DESCENDANT)).map(it -> {
            //((Block) it).getParent()
        });

        // if(firstBlock != null) {
        //     ret 
        // } else {
        //     ret = Optional.emp;
        // }

        // XWiki xWiki;
        // try {
        //     xWiki = XWiki.getXWiki(context);
        // } catch (XWikiException e) {
        //     e.printStackTrace();
        // }
        // TransformationContext txContext = new TransformationContext(xdom, Syntax.PLAIN_1_0);
        // try {
        //     this.transformation.transform(xdom, txContext);
        // } catch (TransformationException e) {
        //     e.printStackTrace();
        // }
        //
        // WikiPrinter printer = new DefaultWikiPrinter();
        // this.renderer.render(xdom, printer);
        // return printer.toString();
    }

    private BlockMatcher findMatchingMention(String anchorId)
    {
        return block -> {
            boolean ret;
            if (block instanceof MacroBlock) {
                ret = ((MacroBlock) block).getId().equals("mention")
                          && Optional.ofNullable(block.getParameter("anchor"))
                                 .map(it -> it.equals(anchorId)).orElse(false);
            } else {
                ret = false;
            }
            return ret;
        };
    }
}
