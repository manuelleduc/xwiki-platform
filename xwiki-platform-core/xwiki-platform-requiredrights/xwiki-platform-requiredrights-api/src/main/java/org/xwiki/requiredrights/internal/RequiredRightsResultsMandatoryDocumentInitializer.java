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
package org.xwiki.requiredrights.internal;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * @version $Id$
 * @since x.y.z
 */
@Component
@Singleton
@Named("XWiki.RequiredRights.Code.RequiredRightsResultClass")
public class RequiredRightsResultsMandatoryDocumentInitializer extends AbstractMandatoryClassInitializer
{
    /**
     * Local reference of the initialized class.
     */
    public static final LocalDocumentReference LOCAL_DOCUMENT_REFERENCE =
        new LocalDocumentReference(List.of("XWiki", "RequiredRights", "Code"), "RequiredRightsResultClass");

    /**
     * The document reference field id.
     */
    public static final String REFERENCE_FIELD = "reference";

    /**
     * The version field id.
     */
    public static final String VERSION_FIELD = "version";

    /**
     * The required rights field id.
     */
    public static final String REQUIRED_RIGHTS_FIELD = "requiredRights";

    /**
     * Default constructor.
     */
    public RequiredRightsResultsMandatoryDocumentInitializer()
    {
        super(LOCAL_DOCUMENT_REFERENCE);
    }

    @Override
    protected void createClass(BaseClass xclass)
    {
        super.createClass(xclass);
        // TODO: localization
        xclass.addTextField(REFERENCE_FIELD, "Reference", 64);
        xclass.addTextField(VERSION_FIELD, "Version", 64);
        xclass.addStaticListField(REQUIRED_RIGHTS_FIELD, "Required Rights", 1, true, "");
    }
}
