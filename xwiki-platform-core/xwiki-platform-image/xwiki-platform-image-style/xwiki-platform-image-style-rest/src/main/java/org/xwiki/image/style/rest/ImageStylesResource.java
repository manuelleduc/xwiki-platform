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
package org.xwiki.image.style.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.xwiki.image.style.ImageStyleException;
import org.xwiki.image.style.rest.model.jaxb.Styles;
import org.xwiki.stability.Unstable;

/**
 * TODO: document me.
 *
 * @version $Id$
 * @since 14.2RC1
 */
@Path("/wikis/{wikiName}/imageStyles")
@Unstable
public interface ImageStylesResource
{
    /**
     * Return the list of styles for a given wiki.
     *
     * @param wikiName the name of the wiki (e.g., {@code xwiki})
     * @return the list of image styles
     * @throws ImageStyleException in case of error while retrieving the list of styles
     */
    @GET
    @Path("/")
    Styles getStyles(@PathParam("wikiName") String wikiName) throws ImageStyleException;
}
