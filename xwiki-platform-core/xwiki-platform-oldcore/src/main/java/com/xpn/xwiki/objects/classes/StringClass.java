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
package com.xpn.xwiki.objects.classes;

import org.apache.ecs.xhtml.input;
import org.xwiki.xml.XMLUtils;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.internal.xml.XMLAttributeValueFilter;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.meta.PropertyMetaClass;

public class StringClass extends PropertyClass
{
    private static final long serialVersionUID = 1L;

    private static final String XCLASSNAME = "string";

    public StringClass(String name, String prettyname, PropertyMetaClass wclass)
    {
        super(name, prettyname, wclass);
        setSize(30);
    }

    public StringClass(PropertyMetaClass wclass)
    {
        this(XCLASSNAME, "String", wclass);
    }

    public StringClass()
    {
        this(null);
    }

    public int getSize()
    {
        return getIntValue("size");
    }

    public void setSize(int size)
    {
        setIntValue("size", size);
    }

    public boolean isPicker()
    {
        return (getIntValue("picker") == 1);
    }

    public void setPicker(boolean picker)
    {
        setIntValue("picker", picker ? 1 : 0);
    }

    @Override
    public BaseProperty fromString(String value)
    {
        BaseProperty property = newProperty();
        property.setValue(value);
        return property;
    }

    @Override
    public BaseProperty newProperty()
    {
        BaseProperty property = new StringProperty();
        property.setName(getName());
        return property;
    }

    @Override
    public void displayEdit(StringBuffer buffer, String name, String prefix, BaseCollection object, XWikiContext context)
    {
        input input = new input();
        input.setAttributeFilter(new XMLAttributeValueFilter());
        BaseProperty prop = (BaseProperty) object.safeget(name);
        if (prop != null) {
            input.setValue(prop.toText());
        }

        input.setType("text");
        input.setName(prefix + name);
        input.setID(prefix + name);
        input.setSize(getSize());
        input.setDisabled(isDisabled());

        if (isPicker()) {
            input.setClass("suggested");
            String path = "";
            XWiki xwiki = context.getWiki();
            path = xwiki.getURL("Main.WebHome", "view", context);

            String classname = this.getObject().getName();
            String fieldname = this.getName();
            String secondCol = "-", firstCol = "-";

            String script =
                "\"" + path + "?xpage=suggest&classname=" + classname + "&fieldname=" + fieldname + "&firCol="
                    + firstCol + "&secCol=" + secondCol + "&\"";
            String varname = "\"input\"";
            input.setOnFocus("new ajaxSuggest(this, {script:" + script + ", varname:" + varname + "} )");
        }

        buffer.append(input.toString());
    }

    @Override
    public void displayView(StringBuffer buffer, String name, String prefix, BaseCollection object,
        XWikiContext context)
    {
        String content = getPropertyText((BaseProperty) object.safeget(name));
        buffer.append(XMLUtils.escapeElementText(content));
    }

    /**
     * Get the text of the base property. Returns the empty string if the base property is null.
     * @param property the base property
     * @return the text of the base property. The empty string is returned if the base property is null
     */
    protected String getPropertyText(BaseProperty property)
    {
        String ret;
        if (property != null) {
            ret = property.toText();
        } else {
            ret = "";
        }
        return ret;
    }
}
