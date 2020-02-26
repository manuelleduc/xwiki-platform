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
package org.xwiki.user.internal.document;

import org.junit.jupiter.api.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.user.User;

import com.xpn.xwiki.XWikiContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CurrentUserResolver}.
 *
 * @version $Id$
 */
@ComponentTest
public class CurrentUserResolverTest
{
    @InjectMockComponents
    private CurrentUserResolver resolver;

    @MockComponent
    private Execution execution;

    @Test
    void resolve()
    {
        DocumentReference reference = new DocumentReference("wiki", "space", "currentuser");
        ExecutionContext executionContext = new ExecutionContext();
        XWikiContext xcontext = mock(XWikiContext.class);
        when(xcontext.getUserReference()).thenReturn(reference);
        executionContext.setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, xcontext);
        when(this.execution.getContext()).thenReturn(executionContext);

        User user = this.resolver.resolve(null);
        assertNotNull(user);
        assertEquals("wiki:space.currentuser", ((DocumentUser) user).getUserReference().getReference().toString());
    }
}
