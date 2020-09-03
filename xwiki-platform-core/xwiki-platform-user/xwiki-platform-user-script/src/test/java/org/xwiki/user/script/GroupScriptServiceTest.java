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
package org.xwiki.user.script;

import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.user.group.GroupManager;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link GroupScriptService}.
 *
 * @version $Id$
 * @since 12.8RC1
 */
@ComponentTest
class GroupScriptServiceTest
{
    private static final DocumentReference CANDIDATE_GROUP = new DocumentReference("xwiki", "XWiki", "Added");

    private static final DocumentReference TARGET_GROUP = new DocumentReference("xwiki", "XWiki", "Target");

    @InjectMockComponents
    private GroupScriptService groupScriptService;

    @Test
    void canAddAsMember() throws Exception
    {
        when(this.groupManager.getMembers(CANDIDATE_GROUP,  true)).thenReturn(emptyList());
        when(this.groupManager.getMembers(TARGET_GROUP, true)).thenReturn(emptyList());

        boolean actual = this.groupScriptService.canAddAsMember(TARGET_GROUP, CANDIDATE_GROUP);
        assertTrue(actual);
    }

    @MockComponent
    private GroupManager groupManager;

    @Test
    void canAddAsMemberTargetNull() throws Exception
    {
        boolean actual = this.groupScriptService.canAddAsMember(null, CANDIDATE_GROUP);
        assertFalse(actual);
        verify(this.groupManager, never()).getGroups(any(), any(), anyBoolean());
        verify(this.groupManager, never()).getMembers(any(), anyBoolean());
    }

    @Test
    void canAddAsMemberCandidateNull() throws Exception
    {
        boolean actual = this.groupScriptService.canAddAsMember(TARGET_GROUP, null);
        assertFalse(actual);
        verify(this.groupManager, never()).getGroups(any(), any(), anyBoolean());
        verify(this.groupManager, never()).getMembers(any(), anyBoolean());
    }

    @Test
    void canAddAsMemberTargetIsCandidate() throws Exception
    {
        boolean actual = this.groupScriptService.canAddAsMember(TARGET_GROUP, TARGET_GROUP);
        assertFalse(actual);
        // if the target is equal to the member no need to do more advanced verifications
        verify(this.groupManager, never()).getGroups(any(), any(), anyBoolean());
        verify(this.groupManager, never()).getMembers(any(), anyBoolean());
    }

    @Test
    void canAddAsMemberCandidateIsAlreadyAMemberOfTarget() throws Exception
    {
        when(this.groupManager.getMembers(TARGET_GROUP, false)).thenReturn(singletonList(CANDIDATE_GROUP));

        boolean actual = this.groupScriptService.canAddAsMember(TARGET_GROUP, CANDIDATE_GROUP);

        assertFalse(actual);
        verify(this.groupManager, never()).getMembers(eq(CANDIDATE_GROUP), anyBoolean());
    }

    @Test
    void canAddAsMemberLinearHierarchy() throws Exception
    {
        /*
        - B is a member of A
        - C is a member of B
        - C can be added to the members of A
        - B cannot be added to the members of C
        - A cannot be added to the members of C
         */

        DocumentReference groupA = new DocumentReference("xwiki", "XWiki", "A");
        DocumentReference groupB = new DocumentReference("xwiki", "XWiki", "B");
        DocumentReference groupC = new DocumentReference("xwiki", "XWiki", "C");

        when(this.groupManager.getMembers(groupA, false)).thenReturn(singletonList(groupB));
        when(this.groupManager.getMembers(groupA, true)).thenReturn(asList(groupB, groupC));

        when(this.groupManager.getMembers(groupB, false)).thenReturn(singletonList(groupC));
        when(this.groupManager.getMembers(groupB, true)).thenReturn(singletonList(groupC));

        when(this.groupManager.getMembers(groupC, false)).thenReturn(emptyList());
        when(this.groupManager.getMembers(groupC, true)).thenReturn(emptyList());

        assertTrue(this.groupScriptService.canAddAsMember(groupA, groupC));
        assertFalse(this.groupScriptService.canAddAsMember(groupC, groupA));
        assertFalse(this.groupScriptService.canAddAsMember(groupC, groupB));
    }

    @Test
    void canAddAsMemberNonLinearHierarchy() throws Exception
    {
        /*
        - B is a member of A
        - C is a member of A
        - C is a member of B
        - C cannot be added to the members of A
        - C cannot be added to the members of B
        - B cannot be added to the members of C
        - A cannot be added to the members of C
         */

        DocumentReference groupA = new DocumentReference("xwiki", "XWiki", "A");
        DocumentReference groupB = new DocumentReference("xwiki", "XWiki", "B");
        DocumentReference groupC = new DocumentReference("xwiki", "XWiki", "C");

        when(this.groupManager.getMembers(groupA, false)).thenReturn(asList(groupB, groupC));
        when(this.groupManager.getMembers(groupA, true)).thenReturn(asList(groupB, groupC));

        when(this.groupManager.getMembers(groupB, false)).thenReturn(singletonList(groupC));
        when(this.groupManager.getMembers(groupB, true)).thenReturn(singletonList(groupC));

        when(this.groupManager.getMembers(groupC, false)).thenReturn(emptyList());
        when(this.groupManager.getMembers(groupC, true)).thenReturn(emptyList());

        assertFalse(this.groupScriptService.canAddAsMember(groupA, groupC));
        assertFalse(this.groupScriptService.canAddAsMember(groupB, groupC));
        assertFalse(this.groupScriptService.canAddAsMember(groupC, groupB));
        assertFalse(this.groupScriptService.canAddAsMember(groupC, groupA));
    }
}
