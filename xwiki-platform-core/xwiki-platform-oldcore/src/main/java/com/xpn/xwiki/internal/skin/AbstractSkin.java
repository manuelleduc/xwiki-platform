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
package com.xpn.xwiki.internal.skin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.skin.Resource;
import org.xwiki.skin.ResourceRepository;
import org.xwiki.skin.Skin;

/**
 * @version $Id$
 * @since 6.4M1
 */
public abstract class AbstractSkin implements Skin
{
    protected static final String ALIASES_KEY = "aliases";

    protected Skin VOID = new Skin()
    {
        @Override
        public Resource<?> getResource(String resource)
        {
            return null;
        }

        @Override
        public Resource<?> getLocalResource(String resource)
        {
            return null;
        }

        @Override
        public Skin getParent()
        {
            return null;
        }

        @Override
        public String getId()
        {
            return null;
        }

        @Override
        public Syntax getOutputSyntax()
        {
            return null;
        }

        @Override
        public Set<String> getAliases(String uixpId)
        {
            return new HashSet<>();
        }
    };

    protected InternalSkinManager skinManager;

    protected InternalSkinConfiguration configuration;

    protected String id;

    protected Skin parent;

    private final Logger logger;

    public AbstractSkin(String id, InternalSkinManager skinManager, InternalSkinConfiguration configuration,
        Logger logger)
    {
        this.id = id;
        this.skinManager = skinManager;
        this.configuration = configuration;
        this.logger = logger;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public Skin getParent()
    {
        if (this.parent == null) {
            this.parent = createParent();

            if (this.parent == null) {
                this.parent = this.skinManager.getSkin(this.configuration.getDefaultParentSkinId());
            }
        }

        return this.parent;
    }

    @Override
    public Resource<?> getResource(String resourceName)
    {
        Resource<?> resource = getLocalResource(resourceName);

        if (resource == null) {
            // Make sure to not try several times the same skin
            Set<String> skins = new HashSet<String>();
            skins.add(getId());
            for (ResourceRepository parentRepo = getParent(); parentRepo != null && resource == null
                && !skins.contains(parentRepo.getId()); parentRepo = parentRepo.getParent()) {
                resource = parentRepo.getLocalResource(resourceName);
                skins.add(parentRepo.getId());
            }
        }

        return resource;
    }

    protected abstract Skin createParent();

    @Override
    public Syntax getOutputSyntax()
    {
        Syntax targetSyntax = null;
        String targetSyntaxString = getOutputSyntaxString();
        if (StringUtils.isNotEmpty(targetSyntaxString)) {
            targetSyntax = parseSyntax(this, targetSyntaxString);
            if (targetSyntax != null) {
                return targetSyntax;
            }
        }

        Skin parentSkin = getParent();
        if (parentSkin != null) {
            targetSyntax = parentSkin.getOutputSyntax();
        }

        // Fallback to the XHTML 1.0 syntax for backward compatibility
        return targetSyntax != null ? targetSyntax : Syntax.XHTML_1_0;
    }

    @Override
    public Set<String> getAliases(String uixpId)
    {
        Skin parentSkin = getParent();
        Set<String> ret;
        if (parentSkin != null) {
            ret = parentSkin.getAliases(uixpId);
        } else {
            ret = new HashSet<>();
        }

        String aliasesString = getAliasesString(uixpId);
        if (aliasesString != null) {
            ret.addAll(Arrays.stream(aliasesString.split(","))
                           .map(String::trim)
                           .filter(it -> !StringUtils.isBlank(it))
                           .collect(Collectors.toSet()));
        }

        return ret;
    }

    /**
     * @return the id of the syntax to use for this skin
     */
    protected abstract String getOutputSyntaxString();

    /**
     * The list of aliases of the uixp in raw form, each value separated by a comma.
     *  
     * @param uixpId the user interface extension point identifier
     * @return the aliases of the uixp, in raw form, each value separated by a comma.
     */
    protected abstract String getAliasesString(String uixpId);

    private Syntax parseSyntax(Skin skin, String syntax)
    {
        try {
            return Syntax.valueOf(syntax);
        } catch (ParseException e) {
            this.logger.warn("Failed to parse the syntax [{}] configured by the skin [{}].", syntax, skin.getId());
        }

        // let getOutputSyntax() do the proper fallback
        return null;
    }
}
