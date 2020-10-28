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
package org.xwiki.user.internal.search;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.user.ActorSearchResult;
import org.xwiki.user.ActorsSearchProvider;
import org.xwiki.user.UserConfiguration;

/**
 * Implementation of {@link ActorsSearchProvider} for the wiki user search.
 * <p>
 * Finds the default {@link ActorsSearchProvider} based on the configured User store hint.
 */
@Component
@Singleton
@Named("user")
public class UserSearchProvider implements ActorsSearchProvider, Initializable
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private UserConfiguration userConfiguration;

    private ActorsSearchProvider actorsSearchProvider;

    @Override
    public List<ActorSearchResult> search(WikiReference wiki, String input)
    {
        return this.actorsSearchProvider.search(wiki, input);
    }

    @Override
    public void initialize() throws InitializationException
    {
        String roleHint = String.format("user/%s", this.userConfiguration.getStoreHint());
        try {
            this.actorsSearchProvider = this.componentManager.getInstance(ActorsSearchProvider.class, roleHint);
        } catch (ComponentLookupException e) {
            throw new InitializationException(
                String.format("Unable to resolve [%s] with hint [%s]", ActorsSearchProvider.class, roleHint), e);
        }
    }
}
