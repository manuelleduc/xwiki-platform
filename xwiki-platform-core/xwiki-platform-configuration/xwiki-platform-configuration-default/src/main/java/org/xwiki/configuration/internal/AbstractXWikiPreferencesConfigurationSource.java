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
package org.xwiki.configuration.internal;

import org.xwiki.model.reference.LocalDocumentReference;

/**
 * Base class for XWiki.XWikiPreferences xclass based configuration.
 * 
 * @version $Id$
 * @since 6.3M1
 */
public abstract class AbstractXWikiPreferencesConfigurationSource extends AbstractXClassConfigurationSource
{
    /**
     * The name of the space where wiki preferences are located.
     */
    protected static final String CLASS_SPACE_NAME = "XWiki";

    protected static final String CLASS_PAGE_NAME = "XWikiPreferences";

    /**
     * The local reference of the class containing wiki preferences.
     */
    protected static final LocalDocumentReference CLASS_REFERENCE = new LocalDocumentReference(CLASS_SPACE_NAME,
        CLASS_PAGE_NAME);

    @Override
    protected LocalDocumentReference getClassReference()
    {
        return CLASS_REFERENCE;
    }
}
