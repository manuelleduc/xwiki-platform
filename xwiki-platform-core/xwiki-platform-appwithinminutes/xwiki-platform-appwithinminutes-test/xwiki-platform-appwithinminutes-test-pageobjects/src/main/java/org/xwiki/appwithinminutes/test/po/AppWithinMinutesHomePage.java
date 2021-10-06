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
package org.xwiki.appwithinminutes.test.po;

import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.livedata.test.po.LiveDataElement;
import org.xwiki.livedata.test.po.TableLayoutElement;
import org.xwiki.test.ui.po.ConfirmationPage;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Represents the actions possible on the App Within Minutes home page.
 * 
 * @version $Id$
 * @since 4.2M1
 */
public class AppWithinMinutesHomePage extends ViewPage
{
    @FindBy(xpath = "//a[@class = 'button' and . = 'Create Application']")
    private WebElement createAppButton;

    /**
     * The Live Data that lists the existing applications.
     */
    private LiveDataElement liveDataElement;

    /**
     * Opens the App Within Minutes home page.
     * 
     * @return the App Within Minutes home page
     */
    public static AppWithinMinutesHomePage gotoPage()
    {
        getUtil().gotoPage("AppWithinMinutes", "WebHome");
        return new AppWithinMinutesHomePage();
    }

    /**
     * @return the URL of the App Within Minutes home page
     */
    public String getURL()
    {
        return getUtil().getURL("AppWithinMinutes", "WebHome");
    }

    /**
     * Clicks on the Create Application button.
     * 
     * @return the page that represents the first step of the App Within Minutes wizard
     */
    public ApplicationCreatePage clickCreateApplication()
    {
        createAppButton.click();
        return new ApplicationCreatePage();
    }

    /**
     * @return the live table that list existing applications
     */
    public LiveDataElement getLiveData()
    {
        if (this.liveDataElement == null) {
            this.liveDataElement = new LiveDataElement("livetable");
        }
        return this.liveDataElement;
    }

    /**
     * Delete the specified application.
     *
     * @param applicationName the application name (for instance, {@code "Movies"})
     * @return this page
     */
    public ConfirmationPage clickDeleteApplication(String applicationName)
    {
        TableLayoutElement tableLayout = getLiveData().getTableLayout();
        tableLayout.filterColumn("Application", applicationName);
        return getApplicationRowIndex(applicationName)
            .map(applicationIndex -> {
                tableLayout.clickAction(applicationIndex, "action_delete");
                return new ConfirmationPage();
            }).orElse(null);
    }

    /**
     * 
     * @param applicationName
     * @return
     */
    public ApplicationClassEditPage editApplication(String applicationName)
    {
        TableLayoutElement tableLayout = getLiveData().getTableLayout();
        tableLayout.filterColumn("Application", applicationName);
        return getApplicationRowIndex(applicationName).map(applicationIndex -> {
            tableLayout.clickAction(applicationIndex, "action_edit");
            return new ApplicationClassEditPage();
        }).orElse(null);
    }

    /**
     * Verify if the requested application name is listed in the Live Data.
     *
     * @param appName the application name (for instance, {@code "Movies"})
     * @return {@code true} if the request application name is found, {@code false} otherwise
     */
    public boolean isApplicationListed(String appName)
    {
        return getApplicationRowIndex(appName).isPresent();
    }

    /**
     * Go to an application by clicking on its link
     *
     * @param appName the application name
     * @return the application home page of the application
     */
    public ApplicationHomePage viewApplication(String appName)
    {
        return getApplicationRowIndex(appName)
            .map(applicationIndex -> {
                WebElement application = getLiveData().getTableLayout().getCell("Application", applicationIndex);
                application.findElement(By.cssSelector("a")).click();
                return new ApplicationHomePage();
            })
            .orElse(null);
    }

    public String getApplicationNameFilter()
    {
        return this.getLiveData().getTableLayout().getFilterValue("Application");
    }

    public boolean canEditApplication(String appName)
    {
        return canDoAction(appName, "a.action_edit");
    }

    public boolean canDeleteApplication(String appName)
    {
        return canDoAction(appName, "a.action_delete");
    }

    /**
     * Look for the index of the request application name.
     *
     * @param appName the application name (for instance, {@code "Movies")
     * @return the row index if the requested application if found, otherwise return {@link Optional#empty()}
     */
    private Optional<Integer> getApplicationRowIndex(String appName)
    {
        TableLayoutElement tableLayout = getLiveData().getTableLayout();
        for (int rowNumber = 1; rowNumber <= tableLayout.countRows(); rowNumber++) {
            WebElement application = tableLayout.getCell("Application", rowNumber);
            if (application.getText().equals(appName)) {
                return Optional.of(rowNumber);
            }
        }
        return Optional.empty();
    }

    private Boolean canDoAction(String appName, String cssSelector)
    {
        return getApplicationRowIndex(appName).map(applicationIndex -> {
            WebElement actions = getLiveData().getTableLayout().getCell("Actions", applicationIndex);
            return actions.findElements(By.cssSelector(cssSelector)).size() == 1;
        }).orElse(false);
    }
}
