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
package org.xwiki.messagestream.test.ui;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.xwiki.administration.test.po.AdministrationPage;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ObjectPropertyReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.platform.notifications.test.po.NotificationsTrayPage;
import org.xwiki.platform.notifications.test.po.NotificationsUserProfilePage;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.docker.junit5.servletengine.ServletEngine;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.SuggestInputElement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.By.cssSelector;
import static org.xwiki.platform.notifications.test.po.NotificationsTrayPage.waitOnNotificationCount;
import static org.xwiki.test.ui.po.BootstrapSwitch.State.ON;

@UITest(servletEngine = ServletEngine.EXTERNAL,
    properties = {
        // Required for filters preferences
        "xwikiDbHbmCommonExtraMappings=notification-filter-preferences.hbm.xml"
    },
    extraJARs = {
        // It's currently not possible to install a JAR contributing a Hibernate mapping file as an Extension. Thus
        // we need to provide the JAR inside WEB-INF/lib. See https://jira.xwiki.org/browse/XWIKI-8271
        "org.xwiki.platform:xwiki-platform-notifications-filters-default",
        // It's currently not possible to install a JAR contributing a Hibernate mapping file as an Extension. Thus
        // we need to provide the JAR inside WEB-INF/lib. See https://jira.xwiki.org/browse/XWIKI-8271
        "org.xwiki.platform:xwiki-platform-eventstream-store-hibernate",
        // The Solr store is not ready yet to be installed as extension
        "org.xwiki.platform:xwiki-platform-eventstream-store-solr"
    }, resolveExtraJARs = true)
class MessageStreamTest
{
    private static final DocumentReference MESSAGESTREAM_CONFIGURATION_DOCUMENT =
        new DocumentReference("xwiki", "XWiki", "MessageStreamConfig");

    private static final String MESSAGESTREAM_CONFIGURATION_XCLASS = "XWiki.MessageStreamConfig";

    private static final String PASSWORD_USERS = "password";

    private static final String USERNAME_U1 = "U1";

    private static final String USERNAME_U2 = "U2";

    private static final String USERNAME_U3 = "U3";

    private static final String USERNAME_SENDER = "SENDER";

    private static final String APPLICATION_ID = "org.xwiki.platform:xwiki-platform-messagestream-api";

    private static final String ALTER_FORMAT = "alert";

    /**
     * Increased timeout for waiting to receive mention notifications.
     */
    private static final int NOTIFICATIONS_COUNT_TIMEOUT = 15;

    @Test
    @Order(1)
    void verifyGlobalAndSpaceAdministrationSections(TestUtils setup)
    {
        AdministrationPage wikiAdministrationPage = AdministrationPage.gotoPage();

        // The MessageStream section should be present
        assertTrue(wikiAdministrationPage.hasSection("MessageStream"));

        // Go to a space's administration page (we use the XWiki space).
        AdministrationPage spaceAdministrationPage = AdministrationPage.gotoSpaceAdministrationPage("XWiki");

        // The MessageStream section should not be present since it's only a wiki level option
        assertTrue(spaceAdministrationPage.hasNotSection("MessageStream"));
    }

    @Test
    @Order(2)
    void verifyDeactivated(TestUtils setup) throws Exception
    {
        setup.loginAsSuperAdmin();

        Object active = setup.rest().get(new ObjectPropertyReference("active",
            new ObjectReference(MESSAGESTREAM_CONFIGURATION_XCLASS, MESSAGESTREAM_CONFIGURATION_DOCUMENT)));

        assertNotNull(active);
    }

    @Test
    @Order(3)
    void setup(TestUtils setup) throws Exception
    {
        /*
         * Create three users U1, U2 and U3
         * Create group G1
         * Add U1 to G1
         * Activate the message stream
         *
         */
        runAsSuperAdmin(setup, () -> {
            setup.createUser(USERNAME_U1, PASSWORD_USERS, null);
            setup.createUser(USERNAME_U2, PASSWORD_USERS, null);
            setup.createUser(USERNAME_U3, PASSWORD_USERS, null);
            setup.createUser(USERNAME_SENDER, PASSWORD_USERS, null);
            setup.loginAsSuperAdmin();

            setup.createPage("XWiki", "G1", null, null);
            setup.addObject("XWiki", "G1", "XWiki.XWikiGroups");
            setup.addObject("XWiki", "G1", "XWiki.XWikiGroups", "member", "XWiki.U1");

            setup.updateObject("XWiki", "MessageStreamConfig", "XWiki.MessageStreamConfig", 0, "active", 1);
        });

        /*
         * Activate the message stream notifications for the three users
         * Make U3 follow Admin
         */
        runAsUser(setup, USERNAME_U1, () -> {
            NotificationsUserProfilePage.gotoPage(USERNAME_U1)
                .setApplicationState(APPLICATION_ID, ALTER_FORMAT, ON);
            NotificationsTrayPage tray = new NotificationsTrayPage();
            tray.clearAllNotifications();
        });

        runAsUser(setup, USERNAME_U2, () -> {
            NotificationsUserProfilePage.gotoPage(USERNAME_U2)
                .setApplicationState(APPLICATION_ID, ALTER_FORMAT, ON);
            NotificationsTrayPage tray = new NotificationsTrayPage();
            tray.clearAllNotifications();
        });

        runAsUser(setup, USERNAME_U3, () -> {
            NotificationsUserProfilePage.gotoPage(USERNAME_U3)
                .setApplicationState(APPLICATION_ID, ALTER_FORMAT, ON);

            NotificationsTrayPage tray = new NotificationsTrayPage();
            tray.clearAllNotifications();

            // Follow admin
            // TODO: extract to PO
            setup.gotoPage("XWiki", USERNAME_SENDER);
            setup.getDriver().findElement(cssSelector(".notificationWatchUserNotFollowing button")).click();
            setup.getDriver().findElement(cssSelector("a.notificationWatchUserAdd")).click();
        });

        runAsUser(setup, USERNAME_SENDER, () -> {
            // Go to the dashboard and send messages
            setup.gotoPage("Main", "MessageSenderMacro");
            WebElement textarea =
                setup.getDriver().findElement(cssSelector("textarea[name='messagestream_message']"));
            WebElement selectTargets = setup.getDriver().findElement(cssSelector("div.messagestream-tools select"));
            WebElement submitButton = setup.getDriver().findElement(cssSelector("div.messagestream-buttons input"));

            textarea.sendKeys("TO EVERYONE");
            submitButton.click();

            Select select = new Select(selectTargets);
            select.selectByValue("followers");
            textarea.sendKeys("TO FOLLOWERS");
            submitButton.click();

            select.selectByValue("group");
            SuggestInputElement picker =
                new SuggestInputElement(setup.getDriver().findElement(cssSelector(".suggest-groups")));
            picker.sendKeys("G1").waitForSuggestions().selectByIndex(0);
            textarea.sendKeys("TO G1");
            submitButton.click();

            select.selectByValue("user");

            picker = new SuggestInputElement(setup.getDriver().findElement(cssSelector(".suggest-users")));
            sendToUser(textarea, submitButton, picker, USERNAME_U1);
            sendToUser(textarea, submitButton, picker, USERNAME_U2);
            sendToUser(textarea, submitButton, picker, USERNAME_U3);
        });

        runAsUser(setup, USERNAME_U1, () -> {
            int expectedCount = 3;
            setup.gotoPage("XWiki", USERNAME_U1);
            waitOnNotificationCount("xwiki:XWiki." + USERNAME_U1, "xwiki", expectedCount, NOTIFICATIONS_COUNT_TIMEOUT);
            NotificationsTrayPage tray = new NotificationsTrayPage();
            tray.showNotificationTray();
            assertEquals(expectedCount, tray.getNotificationsCount());
            assertEquals(expectedCount, tray.getUnreadNotificationsCount());

            assertEquals("replaceme", tray.getNotificationType(0));
            assertEquals("replaceme", tray.getNotificationType(1));
            assertEquals("replaceme", tray.getNotificationType(2));
        });
    }

    private void sendToUser(WebElement textarea, WebElement submitButton, SuggestInputElement picker, String u1)
    {
        picker.sendKeys(u1).waitForSuggestions().selectByValue("XWiki." + u1);
        textarea.sendKeys("TO " + u1);
        submitButton.click();
    }

    /**
     * A duplicate of {@link Runnable} which allows to throw checked {@link Exception}.
     *
     * @see Runnable
     * @see <a href="https://www.baeldung.com/java-lambda-exceptions">Baeldung's Exceptions in Java 8 Lambda
     *     Expressions</a>.
     */
    @FunctionalInterface
    private interface RunnableErr
    {
        void run() throws Exception;
    }

    /**
     * Login as some user and perform some actions, then logout.
     *
     * @param setup The test setup.
     * @param username The user's login.
     * @param actions The actions to be performed.
     * @throws Exception In case of errors.
     */
    private void runAsUser(TestUtils setup, String username, RunnableErr actions) throws Exception
    {
        setup.login(username, PASSWORD_USERS);
        actions.run();
    }

    /**
     * Login as supermadmin, perform some actions, then logout.
     *
     * @param setup The test setup.
     * @param actions Some actions.
     * @throws Exception In case of error.
     */
    private void runAsSuperAdmin(TestUtils setup, RunnableErr actions) throws Exception
    {
        setup.loginAsSuperAdmin();
        actions.run();
    }
}
