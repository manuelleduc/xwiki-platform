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
package org.xwiki.livetable.test.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.livetable.test.po.LivetablePage;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.ViewPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests related to the livetables.
 *
 * @version $Id$
 * @since 13.0RC1
 */
@UITest
class LivetableIT
{
    /**
     * Tests the display of links in livetable cells.
     */
    @Test
    @Order(1)
    void showLinksOnColumns(TestUtils setup, TestReference reference)
    {
        setup.loginAsSuperAdmin();

        List<String> spaceReferences = reference.getSpaceReferences().stream().map(EntityReference::getName).collect(
            Collectors.toList());
        DocumentReference resultsReference =
            new DocumentReference(reference.getWikiReference().getName(), spaceReferences,
                reference.getName() + "Results");
        String resultsReferenceStr = setup.serializeReference(resultsReference);

        /*
         * 1. Creates a page that returns some content in json
         * 2. Creates a pages that uses these results in a livetable
         * 3. Checks that the content of the column c1 is a link when doc_viewable is not defined or doc_viewable is
         * true
         */

        setup.createPage(
            resultsReference, "{{velocity}}\n"
                + "#if ($request.table == 1)\n"
                + "#jsonResponse({\n"
                + "  'reqNo': $request.reqNo,\n"
                + "  'totalrows': 3,\n"
                + "  'returnedrows': 3,\n"
                + "  'offset': $request.offset,\n"
                + "  'rows': [\n"
                + "    { 'c1': 'A', 'c1_url': '#1' },\n"
                + "    { 'c1': 'B', 'c1_url': '#2', 'doc_viewable': true },\n"
                + "    { 'c1': 'C', 'c1_url': '#3', 'doc_viewable': false }\n"
                + "  ]\n"
                + "})\n"
                + "#end\n"
                + "{{/velocity}}", "Livetable results");
        ViewPage livetable = setup.createPage(reference, "{{velocity}}\n"
            + "#set ($urlDoc = $xwiki.getDocument('" + resultsReferenceStr + "'))\n"
            + "#set ($columns = ['c1'])\n"
            + "#set ($columnsProperties = {\n"
            + "  'c1': { 'type': 'none', 'link': 'auto' }\n"
            + "})\n"
            + "#set($options = {\n"
            + "  'url': \"$urlDoc.getURL('view')?table=1\"\n"
            + "})\n"
            + "#livetable('mylivetable' $columns $columnsProperties $options)\n"
            + "{{/velocity}}", "Livetable");

        LivetablePage livetablePage = LivetablePage.fromId("mylivetable");
        assertEquals(3, livetablePage.countRows());
        assertEquals("A", livetablePage.textInRow(1, 1));
        assertEquals("B", livetablePage.textInRow(2, 1));
        assertEquals("C", livetablePage.textInRow(3, 1));
        assertEquals(livetable.getPageURL() + "#1", livetablePage.linkInRow(1, 1));
        assertEquals(livetable.getPageURL() + "#2", livetablePage.linkInRow(2, 1));
        assertNull(livetablePage.linkInRow(3, 1));
    }
}
