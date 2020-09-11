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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.mentions.MentionsConfiguration;

/**
 * Default implementation of {@link MentionsConfiguration}.
 * The settings are retrieved from the instance of Mentions.ConfigurationClass stored in Mentions.Configuration. 
 *
 * @version $Id$
 * @since 12.5RC1
 */
@Component
@Singleton
public class DefaultMentionsConfiguration implements MentionsConfiguration
{
    private static final String RGB_BLACK_COLOR = "#000000";

    @Inject
    @Named("mentions")
    private ConfigurationSource configuration;

    @Override
    public String getMentionsColor()
    {
        return this.configuration.getProperty("mentionsColor", "#c2c2c2");
    }

    @Override
    public double getMentionsOpacity()
    {
        return this.configuration.getProperty("mentionsOpacity", 0.8);
    }

    @Override
    public String getSelfMentionsColor()
    {
        return this.configuration.getProperty("selfMentionsColor", "#ff0000");
    }

    @Override
    public String getMentionsTextColor()
    {
        return this.configuration.getProperty("mentionsTextColor", RGB_BLACK_COLOR);
    }

    @Override
    public String getSelfMentionsTextColor()
    {
        return this.configuration.getProperty("selfMentionsTextColor", RGB_BLACK_COLOR);
    }

    @Override
    public double getSelfMentionsOpacity()
    {
        return this.configuration.getProperty("selfMentionsOpacity", 0.8);
    }

    @Override
    public boolean isQuoteActivated()
    {
        return this.configuration.getProperty("quoteActivated", false);
    }
}
