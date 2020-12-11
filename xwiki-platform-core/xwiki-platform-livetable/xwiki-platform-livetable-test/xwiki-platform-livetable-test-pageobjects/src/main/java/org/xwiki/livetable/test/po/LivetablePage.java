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
package org.xwiki.livetable.test.po;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents a livetable.
 *
 * @version $Id$
 * @since 13.0RC1
 */
public class LivetablePage extends BaseElement
{
    private final WebElement livetableDisplay;

    private LivetablePage(String livetableId)
    {
        By id = By.id(livetableId + "-display");
        this.getDriver().waitUntilElementIsVisible(id);
        this.livetableDisplay = getDriver().findElement(id);
    }

    /**
     * @param livetableId the identifier of the livetable
     * @return the livetable page object for the given id
     */
    public static LivetablePage fromId(String livetableId)
    {
        return new LivetablePage(livetableId);
    }

    /**
     * @return the number of rows displayed in the livetable
     */
    public int countRows()
    {
        return getDriver().findElementsWithoutWaiting(this.livetableDisplay, By.cssSelector("tbody tr")).size();
    }

    /**
     * Find a cell by its line and column number and returns its text.
     *
     * @param line the cell line number (starts at 1)
     * @param column the cell column number (starts at 1)
     * @return the html text of the selected cell
     */
    public String textInRow(int line, int column)
    {
        WebElement element = elementInCell(line, column);
        return element.getText();
    }

    /**
     * Find a cell by its line and column and returns the href value of the first link found in the cell.
     *
     * @param line the cell line number (starts at 1)
     * @param column the cell column number (starts at 1)
     * @return the value of href attribute the first link of the cell
     */
    public String linkInRow(int line, int column)
    {
        try {
            return getDriver().findElementWithoutWaiting(elementInCell(line, column), By.cssSelector("a"))
                .getAttribute("href");
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private WebElement elementInCell(int line, int column)
    {
        WebElement lineElement =
            getDriver().findElementsWithoutWaiting(this.livetableDisplay, By.cssSelector("tbody tr")).get(line - 1);
        return getDriver().findElementsWithoutWaiting(lineElement, By.cssSelector("td")).get(column - 1);
    }
}
