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
package org.xwiki.livedata.internal.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.livedata.LiveDataConfiguration;
import org.xwiki.livedata.LiveDataConfigurationResolver;
import org.xwiki.livedata.LiveDataSourceManager;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

/**
 * Test of {@link DefaultLiveDataEntryPropertyResource}.
 *
 * @version $Id$
 * @since X.Y.X
 */
@ComponentTest
class DefaultLiveDataEntryPropertyResourceTest
{
    @InjectMockComponents
    private DefaultLiveDataEntryPropertyResource target;

    @MockComponent
    private LiveDataSourceManager liveDataSourceManager;

    @MockComponent
    private LiveDataConfigurationResolver<LiveDataConfiguration> defaultLiveDataConfigResolver;

    private DefaultLiveDataEntryPropertyResource defaultLiveDataEntryPropertyResource;

    @BeforeComponent
    void initComponents(MockitoComponentManager componentManager) throws Exception
    {
        
        // TODO: fix this, the test fails because of that...
        this.defaultLiveDataEntryPropertyResource = componentManager
            .registerMockComponent(DefaultLiveDataEntryPropertyResource.class,
                "org.xwiki.livedata.internal.rest.DefaultLiveDataEntryPropertyResource");
    }

    @Test
    void getProperty() throws Exception
    {
        Object property = this.target.getProperty("sourceIdTest", null, "entryIdTest", "propertyIdTest");
    }

    @Test
    void setProperty()
    {
    }
}