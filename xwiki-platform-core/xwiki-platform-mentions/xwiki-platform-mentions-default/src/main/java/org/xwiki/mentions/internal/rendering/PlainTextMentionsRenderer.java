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
package org.xwiki.mentions.internal.rendering;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.rendering.internal.renderer.xhtml.XHTMLRenderer;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;

/**
 * Please document me.
 *
 * @version $Id$
 * @since X.Y.Z
 */
@Component
@Named("plainmentions/1.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class PlainTextMentionsRenderer extends XHTMLRenderer implements Initializable
{
    /**
     * Used to generate link label when not explicitely provided.
     */
    @Inject
    private LinkLabelGenerator linkLabelGenerator;

    // @Override
    // public void initialize() throws InitializationException
    // {
    //     ListenerChain chain = new ListenerChain();
    //     setListenerChain(chain);
    //
    //     // Construct the listener chain in the right order. Listeners early in the chain are called before listeners
    //     // placed later in the chain.
    //     chain.addListener(this);
    //     chain.addListener(new BlockStateChainingListener(chain));
    //     chain.addListener(new EmptyBlockChainingListener(chain));
    //     chain.addListener(new PlainTextChainingRenderer(this.linkLabelGenerator, chain));
    // }

    @Override public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        this.getPrinter().println("@" + parameters.get("reference")+"#TODO");
        super.onMacro(id, parameters, content, inline);
    }
}
