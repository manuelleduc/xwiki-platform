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
package org.xwiki.extension.index.internal.security;

/**
 * Contains the metadata relative to an analyzed vulnerability.
 *
 * @version $Id$
 * @since 15.6RC1
 */
public class FalsePositive
{
    private String source;

    private String explanation;

    /**
     * Default constructor.
     *
     * @param source the {@code source} of the analysis (e.g., {@code xwiki-platform}, or the name of the extension
     *     for which the analysis has been done)
     * @param explanation the textual explanation, detailing why a given CVE should not be considered as a security
     *     vulnerability in the context of the {@code souce}
     */
    public FalsePositive(String source, String explanation)
    {
        this.source = source;
        this.explanation = explanation;
    }

    public FalsePositive()
    {
        this.source = null;
        this.explanation = null;
    }

    /**
     * @return the {@code source} of the analysis (e.g., {@code xwiki-platform}, or the name of the extension for which
     *     the analysis has been done)
     */
    public String getSource()
    {
        return this.source;
    }

    /**
     * @return the textual explanation, detailing why a given CVE should not be considered as a security vulnerability
     *     in the context of the {@code souce}
     */
    public String getExplanation()
    {
        return this.explanation;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public void setExplanation(String explanation)
    {
        this.explanation = explanation;
    }
}
