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
package org.xwiki.requiredrights.internal;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.internal.migration.AbstractDocumentsMigration;
import org.xwiki.query.QueryException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.store.migration.DataMigrationException;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

import static org.xwiki.query.Query.HQL;

/**
 * @version $Id$
 * @since x.y.z
 */
@Component
@Singleton
@Named(RequiredRightsDataMigration.ID)
public class RequiredRightsDataMigration extends AbstractDocumentsMigration
{
    /**
     * The hint for this component.
     */
    public static final String ID = "RequiredRightsDataMigration";

    @Override
    public String getName()
    {
        return ID;
    }

    @Override
    public String getDescription()
    {
        return "Queue all the document of the wiki for required rights analysis.";
    }

    @Override
    public XWikiDBVersion getVersion()
    {
        return new XWikiDBVersion(150500000);
    }

    @Override
    protected String getTaskType()
    {
        return DefaultRequiredRightsAnalyzerTaskConsumer.ID;
    }

    @Override
    protected List<String> selectDocuments() throws DataMigrationException
    {
        XWikiContext context = getXWikiContext();
        XWiki wiki = context.getWiki();
        String wikiId = context.getWikiId();

        try {
            return wiki.getStore()
                .getQueryManager()
                .createQuery("SELECT doc.fullName from XWikiDocument doc where doc.requiredRightsActivated is false",
                    HQL)
                .setWiki(wikiId)
                .execute();
        } catch (QueryException e) {
            throw new DataMigrationException(
                String.format("Failed retrieve the list of all the documents for wiki [%s].", wikiId), e);
        }
    }
}
