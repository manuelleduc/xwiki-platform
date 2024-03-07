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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.image.style.model.ImageStyle;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Provides the script service operations for the image styles. In particular, it provides helpers to generate the REST
 * endpoint URLs.
 *
 * @version $Id$
 * @since 14.3RC1
 */
@Component
@Singleton
@Named("imageStyle")
public class ImageStyleScriptService implements ScriptService
{
    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ImageStyleConfiguration imageStyleConfiguration;

    @Inject
    private ImageStyleManager imageStyleManager;

    @Inject
    private Logger logger;

    /**
     * @return the image styles rest endpoint path
     */
    public String getDefaultImageStyleRestPath()
    {
        return getImageStylesRestPath() + "/default";
    }

    /**
     * @return the default image style rest endpoint path
     */
    public String getImageStylesRestPath()
    {
        return "/rest/wikis/" + this.contextProvider.get().getWikiId() + "/imageStyles";
    }

    /**
     * Return the default style for a given document.
     *
     * @param documentReference the document reference to get the default style for
     * @return a map of default configuration to apply to an image
     * @since 14.10.22
     * @since 15.10.8
     * @since 16.2.0RC1
     */
    @Unstable
    public Map<String, Object> getDefaultImageStyleConfiguration(String documentReference)
    {
        String wikiName = this.contextProvider.get().getWikiId();
        try {
            String defaultStyle = this.imageStyleConfiguration.getDefaultStyle(wikiName, documentReference);
            Map<String, Object> config = Map.of();
            if (defaultStyle != null) {
                Optional<ImageStyle> first = this.imageStyleManager.getImageStyles(wikiName).stream()
                    .filter(it -> it.getIdentifier().equals(defaultStyle)).findFirst();
                if (first.isPresent()) {
                    ImageStyle imageStyle = first.get();
                    config = new HashMap<>();
                    if (!this.imageStyleConfiguration.getForceDefaultStyle(wikiName, documentReference)) {
                        config.put("type", imageStyle.getType());
                    }
                    config.put("defaultWidth", imageStyle.getDefaultWidth());
                    config.put("defaultHeight", imageStyle.getDefaultHeight());
                    config.put("defaultBorder", imageStyle.getDefaultBorder());
                    config.put("defaultAlignment", imageStyle.getDefaultAlignment());
                    config.put("defaultTextWrap", imageStyle.getDefaultTextWrap());
                }
            }
            return config;
        } catch (ImageStyleException e) {
            this.logger.warn("Failed to access default image style configuration for [{}] on wiki [{}]. Cause: [{}]",
                documentReference, wikiName, getRootCauseMessage(e));
            return Map.of();
        }
    }
}
