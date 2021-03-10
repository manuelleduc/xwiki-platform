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
package org.xwiki.livedata;

/**
 * Hold the response to a request to get the edit form of a property. It contains the {@link #body} of the cell in the
 * form of an html content, and the {@link #dependencies} in the form of a set of tag (css or javascript) to add to the
 * header to make the code of the {@link #body} work.
 *
 * @version $Id$
 * @since 13.2RC1
 */
public class LiveDataEntryStoreEditDescriptor
{
    private String body;

    private String dependencies;

    /**
     * @return the body of the entry in edit mode
     */
    public String getBody()
    {
        return this.body;
    }

    /**
     * @param body the body of the entry in edit mode
     */
    public void setBody(String body)
    {
        this.body = body;
    }

    /**
     * @return the dependencies of the body
     */
    public String getDependencies()
    {
        return this.dependencies;
    }

    /**
     * @param dependencies the dependencies of the body
     */
    public void setDependencies(String dependencies)
    {
        this.dependencies = dependencies;
    }
}
