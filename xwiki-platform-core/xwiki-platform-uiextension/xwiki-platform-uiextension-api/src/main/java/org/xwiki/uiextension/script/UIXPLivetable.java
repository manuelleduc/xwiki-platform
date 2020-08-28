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
package org.xwiki.uiextension.script;

import java.util.List;

/**
 * Holds the data of the livetable holding the UIXP list.
 *
 * @version $Id$
 * @since 12.8RC1
 */
public class UIXPLivetable
{
    private long totalrows;

    private long returnedrows;

    private long offset;

    private List<UIXPDescriptor> rows;

    /**
     *
     * @param totalrows The total of rows of the table
     * @return The current object
     */
    public UIXPLivetable setTotalrows(long totalrows)
    {
        this.totalrows = totalrows;
        return this;
    }

    /**
     *
     * @return The total of rows of the table
     */
    public long getTotalrows()
    {
        return this.totalrows;
    }

    /**
     *
     * @param returnedrows The number of rows in the current table page
     * @return The current object
     */
    public UIXPLivetable setReturnedrows(long returnedrows)
    {
        this.returnedrows = returnedrows;
        return this;
    }

    /**
     *
     * @return The number of rows in the current table page
     */
    public long getReturnedrows()
    {
        return this.returnedrows;
    }

    /**
     *
     * @param offset The table offset
     * @return The current object
     */
    public UIXPLivetable setOffset(long offset)
    {
        this.offset = offset;
        return this;
    }

    /**
     *
     * @return The table offset
     */
    public long getOffset()
    {
        return this.offset;
    }

    /**
     *
     * @param rows The table rows
     * @return the current object
     */
    public UIXPLivetable setRows(List<UIXPDescriptor> rows)
    {
        this.rows = rows;
        return this;
    }

    /**
     *
     * @return The table rows
     */
    public List<UIXPDescriptor> getRows()
    {
        return this.rows;
    }


}
