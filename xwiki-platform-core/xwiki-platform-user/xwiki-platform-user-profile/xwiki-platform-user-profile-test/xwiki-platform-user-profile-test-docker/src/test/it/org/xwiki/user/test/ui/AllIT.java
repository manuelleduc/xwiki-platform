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
package org.xwiki.user.test.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.xwiki.test.docker.junit5.UITest;

/**
 * All UI tests for the user profile.
 *
 * @since 11.10
 * @version $Id$
 */
// Extra JARs needed for the hibernate mapping (since right now we don't support hibernate mappings contributed at
// runtime by extensions.
@UITest(extraJARs = { "org.xwiki.platform:xwiki-platform-eventstream-store" })
public class AllIT
{
    @Nested
    @DisplayName("User Profile Tests")
    class NestedUserProfileIT extends UserProfileIT
    {
    }

    @Nested
    @DisplayName("User Password Changing Tests")
    class NestedUserChangePasswordIT extends UserChangePasswordIT
    {
    }
}
