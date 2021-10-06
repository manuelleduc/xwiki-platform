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
package org.xwiki.appwithinminutes.test.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.appwithinminutes.test.po.AppWithinMinutesHomePage;
import org.xwiki.appwithinminutes.test.po.ApplicationClassEditPage;
import org.xwiki.appwithinminutes.test.po.ApplicationCreatePage;
import org.xwiki.appwithinminutes.test.po.ApplicationHomeEditPage;
import org.xwiki.appwithinminutes.test.po.ApplicationHomePage;
import org.xwiki.appwithinminutes.test.po.ClassFieldEditPane;
import org.xwiki.livedata.test.po.TableLayoutElement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.integration.junit.LogCaptureConfiguration;
import org.xwiki.test.ui.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the live table that lists the existing applications on the AppWithinMinutes home page.
 *
 * @version $Id$
 * @since 11.10
 */
@UITest
class AppsLiveTableIT
{
    private static final String USERNAME = AppsLiveTableIT.class.getSimpleName();
    private static final String PASSWORD = "simplepassword";

    private AppWithinMinutesHomePage appWithinMinutesHomePage;
    private String appName;

    @BeforeAll
    public void setupClass(TestUtils testUtils)
    {
        testUtils.createUserAndLogin(USERNAME, PASSWORD);
    }

    @BeforeEach
    public void setUp(TestReference testReference, TestUtils testUtils, LogCaptureConfiguration logCaptureConfiguration)
    {
        logCaptureConfiguration.registerExpected("WikiComponentException: Registering UI "
            + "extensions at wiki level requires wiki administration rights");

        testUtils.login(USERNAME, PASSWORD);
        this.appName = testReference.getLastSpaceReference().getName();
        appWithinMinutesHomePage = createApplication(appName);
    }

    /**
     * Creates an application with the specified name. The application class will have just one field.
     *
     * @param appName the name of the application to create
     */
    private AppWithinMinutesHomePage createApplication(String appName)
    {
        ApplicationCreatePage appCreatePage = AppWithinMinutesHomePage.gotoPage().clickCreateApplication();
        appCreatePage.setApplicationName(appName);
        ApplicationClassEditPage classEditPage = appCreatePage.clickNextStep();
        classEditPage.addField("Short Text");
        classEditPage.clickNextStep().clickNextStep().clickFinish();
        return AppWithinMinutesHomePage.gotoPage();
    }

    @Order(1)
    @Test
    void deleteApplication()
    {
        // Check the applications Live Data lists of the created application.
        TableLayoutElement liveData = this.appWithinMinutesHomePage.getLiveData().getTableLayout();
        assertTrue(liveData.hasColumn("Actions"));
        String appFilteringValue = this.appName.substring(0, 3);
        liveData.filterColumn("Application", appFilteringValue);
        assertTrue(this.appWithinMinutesHomePage.isApplicationListed(this.appName));

        // Click the delete icon then cancel the confirmation.
        this.appWithinMinutesHomePage.clickDeleteApplication(this.appName).clickNo();
        // We should be taken back to the AppWithinMinutes home page.
        this.appWithinMinutesHomePage = AppWithinMinutesHomePage.gotoPage();
        // The application name filter should've been preserved.
        assertEquals(appFilteringValue, this.appWithinMinutesHomePage.getApplicationNameFilter());

        // Click the delete icon again and this confirm the action.
        this.appWithinMinutesHomePage.clickDeleteApplication(this.appName).clickYes();
        // We should be taken back to the AppWithinMinutes home page.
        this.appWithinMinutesHomePage = AppWithinMinutesHomePage.gotoPage();
        // The application name filter should've been preserved.
        assertEquals(appFilteringValue, this.appWithinMinutesHomePage.getApplicationNameFilter());
        // And the deleted application shouldn't be listed anymore.
        assertFalse(this.appWithinMinutesHomePage.isApplicationListed(this.appName));
    }

    @Order(2)
    @Test
    void testEditApplication()
    {
        // Edit the application.
//        TableLayoutElement liveData = this.appWithinMinutesHomePage.getLiveData().getTableLayout();
        ApplicationClassEditPage classEditor = this.appWithinMinutesHomePage.editApplication(this.appName);

        // Edit the existing class field.
        ClassFieldEditPane fieldEditPane = new ClassFieldEditPane("shortText1");
        fieldEditPane.setPrettyName("City Name");
        fieldEditPane.openConfigPanel();
        fieldEditPane.setName("cityName");

        // Move to the next step.
        ApplicationHomeEditPage homeEditPage = classEditor.clickNextStep().clickNextStep();
        homeEditPage.setDescription("demo");

        // Finish editing.
        ApplicationHomePage homePage = homeEditPage.clickFinish();
        assertTrue(homePage.getContent().contains("demo"));
    }

    @Order(3)
    @Test
    void testActionRights(TestUtils testUtils)
    {
        // set some rights before the test
        DocumentReference xwikiPreferences = new DocumentReference("xwiki", "XWiki", "XWikiPreferences");

        testUtils.loginAsSuperAdmin();
        String anotherUserName = "someOtherUser";
        testUtils.createPage(xwikiPreferences, "");
        testUtils.addObject(xwikiPreferences, "XWiki.XWikiGlobalRights",
            "levels", "edit,script",
            "allow", "1",
            "users", String.format("XWiki.%s,XWiki.%s", USERNAME, anotherUserName));

        testUtils.login(USERNAME, PASSWORD);
        this.appWithinMinutesHomePage = AppWithinMinutesHomePage.gotoPage();
        try {
            // The application author should be able to edit and delete the application.
            TableLayoutElement liveData = this.appWithinMinutesHomePage.getLiveData().getTableLayout();
            liveData.filterColumn("Application", this.appName);

            assertTrue(this.appWithinMinutesHomePage.canEditApplication(this.appName));
            assertTrue(this.appWithinMinutesHomePage.canDeleteApplication(this.appName));

            // Logout. Guests shouldn't be able to edit nor delete the application.
            appWithinMinutesHomePage.logout();
            testUtils.recacheSecretToken();
            appWithinMinutesHomePage = new AppWithinMinutesHomePage();
            liveData = appWithinMinutesHomePage.getLiveData().getTableLayout();
            liveData.filterColumn("Application", this.appName);
            assertFalse(this.appWithinMinutesHomePage.canEditApplication(this.appName));
            assertFalse(this.appWithinMinutesHomePage.canDeleteApplication(this.appName));

            // Login with a different user. The new user shouldn't be able to delete the application.
            testUtils.createUserAndLogin(anotherUserName, "somePassword");
            liveData = AppWithinMinutesHomePage.gotoPage().getLiveData().getTableLayout();
            liveData.filterColumn("Application", this.appName);
            assertTrue(this.appWithinMinutesHomePage.canEditApplication(this.appName));
            assertFalse(this.appWithinMinutesHomePage.canDeleteApplication(this.appName));
        } finally {
            // We don't want to keep the rights
            testUtils.deletePage(xwikiPreferences);
        }
    }
}
