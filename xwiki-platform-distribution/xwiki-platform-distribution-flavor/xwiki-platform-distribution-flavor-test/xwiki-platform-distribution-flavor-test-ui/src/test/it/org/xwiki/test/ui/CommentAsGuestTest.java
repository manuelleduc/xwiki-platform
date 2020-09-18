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
package org.xwiki.test.ui;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.administration.test.po.GlobalRightsAdministrationSectionPage;
import org.xwiki.test.ui.browser.IgnoreBrowser;
import org.xwiki.test.ui.browser.IgnoreBrowsers;
import org.xwiki.test.ui.po.CommentsTab;
import org.xwiki.test.ui.po.EditRightsPane.Right;
import org.xwiki.test.ui.po.EditRightsPane.State;
import org.xwiki.test.ui.po.ViewPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 * @since 3.1M2
 */
public class CommentAsGuestTest extends AbstractTest
{
    @Rule
    public AdminAuthenticationRule adminAuthenticationRule = new AdminAuthenticationRule(getUtil());

    private static final String CONTENT = "Some dummy Content";

    private static final String TITLE = "CommentsTest Page";

    private static final String COMMENT_CONTENT = "Some content";

    private static final String COMMENT_AUTHOR = "Anonymous";

    private static final String COMMENT_REPLY = "Comment Reply";

    private ViewPage vp;

    @BeforeClass
    public static void setup()
    {
        // Ensure that guest user has comment permission
        getUtil().loginAsSuperAdmin();
        setRightsOnGuest(Right.COMMENT, State.ALLOW);
    }

    @Before
    public void setUp() throws Exception
    {
        getUtil().loginAsSuperAdmin();
        getUtil().rest().deletePage(getTestClassName(), getTestMethodName());
        this.vp = getUtil().createPage(getTestClassName(), getTestMethodName(), CONTENT, TITLE);

        // Set Guest user
        getUtil().forceGuestUser();

        // Important: we need to reload the page since forceGuestUser() simply removes the cookies and doesn't refresh
        // the page and we need it refreshed since there's an extra field for guest users when commenting (the user
        // name field).
        getDriver().navigate().refresh();
    }

    private static void setRightsOnGuest(Right right, State state)
    {
        GlobalRightsAdministrationSectionPage globalRights = GlobalRightsAdministrationSectionPage.gotoPage();
        // Wait for the rights live table to load before switching in order to prevent any possible race conditions.
        globalRights.getEditRightsPane().getRightsTable().waitUntilReady();
        globalRights.getEditRightsPane().switchToUsers();
        globalRights.getEditRightsPane().setGuestRight(right, state);
    }

    @Test
    @IgnoreBrowsers({
    @IgnoreBrowser(value = "internet.*", version = "8\\.*", reason="See https://jira.xwiki.org/browse/XE-1146"),
    @IgnoreBrowser(value = "internet.*", version = "9\\.*", reason="See https://jira.xwiki.org/browse/XE-1177")
    })
    public void testPostCommentAsGuest()
    {
        CommentsTab commentsTab = this.vp.openCommentsDocExtraPane();

        commentsTab.postCommentAsGuest(COMMENT_CONTENT, COMMENT_AUTHOR, true);
        assertEquals(COMMENT_CONTENT, commentsTab.getCommentContentByID(0));
        assertEquals(COMMENT_AUTHOR, commentsTab.getCommentAuthorByID(0));
    }

    @Test
    @IgnoreBrowsers({
    @IgnoreBrowser(value = "internet.*", version = "8\\.*", reason="See https://jira.xwiki.org/browse/XE-1146"),
    @IgnoreBrowser(value = "internet.*", version = "9\\.*", reason="See https://jira.xwiki.org/browse/XE-1177")
    })
    public void testReplyCommentAsAnonymous()
    {
        CommentsTab commentsTab = this.vp.openCommentsDocExtraPane();

        int commentId = commentsTab.postCommentAsGuest(COMMENT_CONTENT, COMMENT_AUTHOR, true);
        commentsTab.replyToCommentByID(commentId, COMMENT_REPLY);
        assertEquals(COMMENT_REPLY, commentsTab.getCommentContentByID(commentId));
        assertEquals(COMMENT_AUTHOR, commentsTab.getCommentAuthorByID(commentId));
    }

    @Test
    @IgnoreBrowsers({
    @IgnoreBrowser(value = "internet.*", version = "8\\.*", reason="See https://jira.xwiki.org/browse/XE-1146"),
    @IgnoreBrowser(value = "internet.*", version = "9\\.*", reason="See https://jira.xwiki.org/browse/XE-1177")
    })
    public void testCannotEditCommentAsAnonymous()
    {
        CommentsTab commentsTab = this.vp.openCommentsDocExtraPane();
        int commentId = commentsTab.postCommentAsGuest(COMMENT_CONTENT, COMMENT_AUTHOR, true);
        assertFalse(commentsTab.hasEditButtonForCommentByID(commentId));
    }
}
