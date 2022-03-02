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
package org.xwiki.image.style.rest.internal;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.image.style.ImageStyleConfiguration;
import org.xwiki.image.style.ImageStyleException;
import org.xwiki.image.style.ImageStyleManager;
import org.xwiki.image.style.model.ImageStyle;
import org.xwiki.image.style.rest.ImageStylesResource;
import org.xwiki.image.style.rest.model.jaxb.Style;
import org.xwiki.image.style.rest.model.jaxb.Styles;
import org.xwiki.rest.XWikiRestComponent;

/**
 * TODO: document me.
 *
 * @version $Id$
 * @since 14.2RC1
 */
@Component
@Named("org.xwiki.image.style.rest.internal.DefaultImageStylesResource")
@Singleton
public class DefaultImageStylesResource implements ImageStylesResource, XWikiRestComponent
{
    @Inject
    private ImageStyleConfiguration imageStyleConfiguration;

    @Inject
    private ImageStyleManager imageStyleManager;

    @Override
    public Styles getStyles(String wikiName) throws ImageStyleException
    {
        Styles styles = new Styles();
        styles.setDefault(this.imageStyleConfiguration.getDefaultStyle());
        styles.getImageStyles().addAll(convert(this.imageStyleManager.getImageStyles(wikiName)));
        return styles;
    }

    private List<Style> convert(Set<ImageStyle> imageStyles)
    {
        return imageStyles
            .stream()
            .map(this::convert)
            .collect(Collectors.toList());
    }

    private Style convert(ImageStyle imageStyle)
    {
        Style style = new Style();
        style.setClazz(imageStyle.getType());
        style.setId(imageStyle.getIdentifier());
        style.setPrettyName(imageStyle.getPrettyName());
        return style;
    }
}
