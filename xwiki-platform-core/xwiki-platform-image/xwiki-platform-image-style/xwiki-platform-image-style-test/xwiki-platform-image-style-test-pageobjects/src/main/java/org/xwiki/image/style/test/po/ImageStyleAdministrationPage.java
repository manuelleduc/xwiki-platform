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
package org.xwiki.image.style.test.po;

import java.util.Map;

import org.openqa.selenium.By;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.ui.XWikiWebDriver;
import org.xwiki.test.ui.po.FormContainerElement;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Page object of the image style administration.
 *
 * @version $Id$
 * @since 14.2RC1
 */
public class ImageStyleAdministrationPage extends ViewPage
{
    /**
     * @return the page object for the administration of the image styles
     */
    public static ImageStyleAdministrationPage getToAdminPage()
    {
        getUtil().gotoPage(new DocumentReference("xwiki", "XWiki", "XWikiPreferences"), "admin", Map.of(
            "editor", "globaladmin",
            "section", "image.style"
        ));
        return new ImageStyleAdministrationPage();
    }

    /**
     * Create a configuration form from an identifier.
     *
     * @param identifier the identifier (e.g., "frameless")
     * @return the page object of the image style configuration form
     */
    public ImageStyleConfigurationForm submitNewImageStyleForm(String identifier)
    {
        XWikiWebDriver driver = getDriver();
        driver.findElementWithoutWaiting(By.id("targetTitle")).sendKeys(identifier);
        driver.findElementWithoutWaiting(By.cssSelector("#newImageStyleForm input[type='submit']")).click();
        return new ImageStyleConfigurationForm();
    }

    /**
     * Select the identifier of the default image style and save it.
     *
     * @param identifier the identifier of the default image style
     */
    public void submitDefaultStyleForm(String identifier)
    {
        String defaultImageStyleFormId = "defaultImageStyleForm";
        new FormContainerElement(By.id(defaultImageStyleFormId))
            .setFieldValue(By.id("Image.Style.Code.ConfigurationClass_0_defaultStyle"), identifier);
        getDriver().findElementById(defaultImageStyleFormId).findElement(By.cssSelector("input[type='submit']"))
            .click();
    }
}
