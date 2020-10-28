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
package org.xwiki.user;

import org.xwiki.stability.Unstable;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Data structure holding a single result of an actor search.
 *
 * @version $Id$
 * @since 12.10RC1
 */
@Unstable
public class ActorSearchResult
{
    private String value;

    private String label;

    private String url;

    private String type;

    public String getValue()
    {
        return this.value;
    }

    public ActorSearchResult setValue(String value)
    {
        this.value = value;
        return this;
    }

    public String getLabel()
    {
        return this.label;
    }

    public ActorSearchResult setLabel(String label)
    {
        this.label = label;
        return this;
    }

    public String getUrl()
    {
        return this.url;
    }

    public ActorSearchResult setUrl(String url)
    {
        this.url = url;
        return this;
    }

    public String getType()
    {
        return this.type;
    }

    public ActorSearchResult setType(String type)
    {
        this.type = type;
        return this;
    }

    @Override
    public String toString()
    {

        return new XWikiToStringBuilder(this)
            .append("value", getValue())
            .append("label", getLabel())
            .append("url", getUrl())
            .append("type", getType())
            .toString();
    }
}
