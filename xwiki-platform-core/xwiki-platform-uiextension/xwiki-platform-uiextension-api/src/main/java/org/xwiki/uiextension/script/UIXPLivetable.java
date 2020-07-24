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
 * Please document me.
 *
 * @version $Id$
 * @since X.Y.Z
 */
public class UIXPLivetable
{
    private long totalrows;

    private long returnedrows;

    private long offset;

    private List<UIXPDescriptor> rows;

    public UIXPLivetable setTotalrows(long totalrows)
    {
        this.totalrows = totalrows;
        return this;
    }

    public long getTotalrows()
    {
        return this.totalrows;
    }

    public UIXPLivetable setReturnedrows(long returnedrows)
    {
        this.returnedrows = returnedrows;
        return this;
    }

    public long getReturnedrows()
    {
        return this.returnedrows;
    }

    public UIXPLivetable setOffset(long offset)
    {
        this.offset = offset;
        return this;
    }

    public long getOffset()
    {
        return this.offset;
    }

    public UIXPLivetable setRows(List<UIXPDescriptor> rows)
    {
        this.rows = rows;
        return this;
    }

    public List<UIXPDescriptor> getRows()
    {
        return this.rows;
    }


}
