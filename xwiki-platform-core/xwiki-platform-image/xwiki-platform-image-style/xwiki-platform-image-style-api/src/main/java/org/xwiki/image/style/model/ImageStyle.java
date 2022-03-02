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
package org.xwiki.image.style.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.stability.Unstable;

/**
 * TODO: document me.
 *
 * @version $Id$
 * @since 14.2RC1
 */
@Unstable
public class ImageStyle
{
    private String prettyName;

    private String identifier;

    private String type;

    public String getPrettyName()
    {
        return this.prettyName;
    }

    public ImageStyle setPrettyName(String prettyName)
    {
        this.prettyName = prettyName;
        return this;
    }

    public String getIdentifier()
    {
        return this.identifier;
    }

    public ImageStyle setIdentifier(String identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public String getType()
    {
        return this.type;
    }

    public ImageStyle setType(String type)
    {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageStyle that = (ImageStyle) o;

        return new EqualsBuilder()
            .append(this.prettyName, that.prettyName)
            .append(this.identifier, that.identifier)
            .append(this.type, that.type)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(this.prettyName)
            .append(this.identifier)
            .append(this.type)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("prettyName", this.prettyName)
            .append("identifier", this.identifier)
            .append("type", this.type)
            .toString();
    }
}
