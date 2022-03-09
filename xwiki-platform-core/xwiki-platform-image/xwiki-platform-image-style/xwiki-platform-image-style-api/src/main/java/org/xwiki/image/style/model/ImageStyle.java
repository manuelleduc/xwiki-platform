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
    private String identifier;

    private String prettyName;

    private String type;

    private Boolean adjustableSize;

    private Long defaultWidth;

    private Long defaultHeight;

    private Boolean adjustableBorder;

    private Boolean defaultBorder;

    private Boolean adjustableAlignment;

    private String defaultAlignment;

    private Boolean adjustableTextWrap;

    private Boolean defaultTextWrap;

    public String getIdentifier()
    {
        return this.identifier;
    }

    public ImageStyle setIdentifier(String identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public String getPrettyName()
    {
        return this.prettyName;
    }

    public ImageStyle setPrettyName(String prettyName)
    {
        this.prettyName = prettyName;
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

    public Boolean getAdjustableSize()
    {
        return this.adjustableSize;
    }

    public ImageStyle setAdjustableSize(Boolean adjustableSize)
    {
        this.adjustableSize = adjustableSize;
        return this;
    }

    public Long getDefaultWidth()
    {
        return this.defaultWidth;
    }

    public ImageStyle setDefaultWidth(Long defaultWidth)
    {
        this.defaultWidth = defaultWidth;
        return this;
    }

    public Long getDefaultHeight()
    {
        return this.defaultHeight;
    }

    public ImageStyle setDefaultHeight(Long defaultHeight)
    {
        this.defaultHeight = defaultHeight;
        return this;
    }

    public Boolean getAdjustableBorder()
    {
        return this.adjustableBorder;
    }

    public ImageStyle setAdjustableBorder(Boolean adjustableBorder)
    {
        this.adjustableBorder = adjustableBorder;
        return this;
    }

    public Boolean getDefaultBorder()
    {
        return this.defaultBorder;
    }

    public ImageStyle setDefaultBorder(Boolean defaultBorder)
    {
        this.defaultBorder = defaultBorder;
        return this;
    }

    public Boolean getAdjustableAlignment()
    {
        return this.adjustableAlignment;
    }

    public ImageStyle setAdjustableAlignment(Boolean adjustableAlignment)
    {
        this.adjustableAlignment = adjustableAlignment;
        return this;
    }

    public String getDefaultAlignment()
    {
        return this.defaultAlignment;
    }

    public ImageStyle setDefaultAlignment(String defaultAlignment)
    {
        this.defaultAlignment = defaultAlignment;
        return this;
    }

    public Boolean getAdjustableTextWrap()
    {
        return this.adjustableTextWrap;
    }

    public ImageStyle setAdjustableTextWrap(Boolean adjustableTextWrap)
    {
        this.adjustableTextWrap = adjustableTextWrap;
        return this;
    }

    public Boolean getDefaultTextWrap()
    {
        return this.defaultTextWrap;
    }

    public ImageStyle setDefaultTextWrap(Boolean defaultTextWrap)
    {
        this.defaultTextWrap = defaultTextWrap;
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
            .append(this.identifier, that.identifier)
            .append(this.prettyName, that.prettyName)
            .append(this.type, that.type)
            .append(this.adjustableSize, that.adjustableSize)
            .append(this.defaultWidth, that.defaultWidth)
            .append(this.defaultHeight, that.defaultHeight)
            .append(this.adjustableBorder, that.adjustableBorder)
            .append(this.defaultBorder, that.defaultBorder)
            .append(this.adjustableAlignment, that.adjustableAlignment)
            .append(this.defaultAlignment, that.defaultAlignment)
            .append(this.adjustableTextWrap, that.adjustableTextWrap)
            .append(this.defaultTextWrap, that.defaultTextWrap)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(this.identifier)
            .append(this.prettyName)
            .append(this.type)
            .append(this.adjustableSize)
            .append(this.defaultWidth)
            .append(this.defaultHeight)
            .append(this.adjustableBorder)
            .append(this.defaultBorder)
            .append(this.adjustableAlignment)
            .append(this.defaultAlignment)
            .append(this.adjustableTextWrap)
            .append(this.defaultTextWrap)
            .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("identifier", this.identifier)
            .append("prettyName", this.prettyName)
            .append("type", this.type)
            .append("adjustableSize", this.adjustableSize)
            .append("defaultWidth", this.defaultWidth)
            .append("defaultHeight", this.defaultHeight)
            .append("adjustableBorder", this.adjustableBorder)
            .append("defaultBorder", this.defaultBorder)
            .append("adjustableAlignment", this.adjustableAlignment)
            .append("defaultAlignment", this.defaultAlignment)
            .append("adjustableTextWrap", this.adjustableTextWrap)
            .append("defaultTextWrap", this.defaultTextWrap)
            .toString();
    }
}
