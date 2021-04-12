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
package org.xwiki.appwithinminutes.test.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.xwiki.appwithinminutes.test.po.ApplicationClassEditPage;
import org.xwiki.appwithinminutes.test.po.SuggestClassFieldEditPane;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.docker.junit5.servletengine.ServletEngine;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.InlinePage;
import org.xwiki.test.ui.po.SuggestInputElement;
import org.xwiki.test.ui.po.SuggestInputElement.SuggestionElement;
import org.xwiki.xclass.test.po.ClassSheetPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xwiki.appwithinminutes.test.po.ApplicationClassEditPage.goToEditor;

/**
 * Special class editor tests that address only the Page class field type.
 *
 * @version $Id$
 * @since 10.6
 */
@UITest(servletEngine = ServletEngine.EXTERNAL)
class PageClassFieldIT
{
    @BeforeEach
    void setUp(TestUtils testUtils, TestReference testReference)
    {
        testUtils.loginAsSuperAdmin();
        String className = testReference.getSpaceReferences().get(0).getName();
        testUtils.deleteSpace(className);
        testUtils.createPage(className, "pageclassfieldpage1", "Content", className + " Page 1");
        testUtils.createPage(className, "pageclassfieldpage2", "Content", className + " Page 2");
        testUtils.createPage(Arrays.asList(className, "space"), "pageclassfieldtesthome", "Content",
            className + " TestHome");
    }

    @Test
    @Order(1)
    void suggestions(TestReference testReference)
    {
        ApplicationClassEditPage editor = goToEditor(testReference);

        String className = testReference.getSpaceReferences().get(0).getName();
        SuggestInputElement pagePicker = new SuggestClassFieldEditPane(editor.addField("Page").getName()).getPicker();

        // Make sure the picker is ready.
        pagePicker.click().waitForSuggestions();

        List<SuggestionElement> suggestions =
            pagePicker.sendKeys(className, " pag").waitForSuggestions().getSuggestions();
        assertEquals(3, suggestions.size());
        assertEquals(className + " Page 1", suggestions.get(0).getLabel());
        assertEquals(className, suggestions.get(0).getHint());
        assertEquals(className + " Page 2", suggestions.get(1).getLabel());
        assertEquals(className, suggestions.get(1).getHint());

        suggestions = pagePicker.sendKeys(" 1").waitForSuggestions().getSuggestions();
        assertEquals(1, suggestions.size());
        assertEquals(className + " Page 1", suggestions.get(0).getLabel());

        suggestions = pagePicker.clear().sendKeys(className).waitForSuggestions().getSuggestions();
        assertEquals(3, suggestions.size());
        assertEquals(className + " Page 1", suggestions.get(0).getLabel());
        assertEquals(className, suggestions.get(0).getHint());
        assertEquals(className + " Page 2", suggestions.get(1).getLabel());
        assertEquals(className, suggestions.get(1).getHint());
        assertEquals(className + " TestHome", suggestions.get(2).getLabel());
        assertEquals(className + " / space", suggestions.get(2).getHint());
    }

    @Test
    @Order(2)
    void singleSelection(TestReference testReference)
    {
        ApplicationClassEditPage editor = goToEditor(testReference);
        String className = testReference.getSpaceReferences().get(0).getName();
        SuggestInputElement pagePicker = new SuggestClassFieldEditPane(editor.addField("Page").getName()).getPicker();

        // Make sure the picker is ready.
        pagePicker.click().waitForSuggestions();

        // Use the keyboard.
        List<SuggestionElement> selectedPages = pagePicker.sendKeys(className).waitForSuggestions().sendKeys(Keys.ENTER)
            .getSelectedSuggestions();
        assertEquals(1, selectedPages.size());
        assertEquals(className + " Page 1", selectedPages.get(0).getLabel());
        assertEquals(Collections.singletonList(className + ".pageclassfieldpage1"), pagePicker.getValues());

        // Use the mouse. Since we have single selection by default, the previously selected page should be replaced.
        selectedPages = pagePicker.click().selectByVisibleText(className + " Page 2").getSelectedSuggestions();
        assertEquals(1, selectedPages.size());
        assertEquals(className + " Page 2", selectedPages.get(0).getLabel());
        assertEquals(Collections.singletonList(className + ".pageclassfieldpage2"), pagePicker.getValues());

        // Delete the selected page.
        selectedPages.get(0).delete();
        assertEquals(0, pagePicker.getSelectedSuggestions().size());
        assertEquals(Collections.singletonList(""), pagePicker.getValues());
    }

    @Test
    @Order(3)
    void multipleSelection(TestReference testReference)
    {
        ApplicationClassEditPage editor = goToEditor(testReference);
        String className = testReference.getSpaceReferences().get(0).getName();
        SuggestClassFieldEditPane pageField = new SuggestClassFieldEditPane(editor.addField("Page").getName());
        pageField.openConfigPanel();
        pageField.setMultipleSelect(true);
        pageField.closeConfigPanel();
        SuggestInputElement pagePicker = pageField.getPicker();

        // Make sure the picker is ready.
        pagePicker.click().waitForSuggestions();

        // Select 2 pages.
        List<SuggestionElement> selectedPages = pagePicker.sendKeys(className).waitForSuggestions().sendKeys(Keys.ENTER)
            .sendKeys(Keys.ENTER).getSelectedSuggestions();
        assertEquals(2, selectedPages.size());
        assertEquals(className + " Page 1", selectedPages.get(0).getLabel());
        assertEquals(className + " Page 2", selectedPages.get(1).getLabel());
        assertEquals(Arrays.asList(className + ".pageclassfieldpage1", className + ".pageclassfieldpage2"),
            pagePicker.getValues());

        // Delete the first selected page.
        selectedPages.get(0).delete();

        // Select another page.
        pagePicker.sendKeys(className, " testhome").waitForSuggestions().sendKeys(Keys.ENTER);
        selectedPages = pagePicker.getSelectedSuggestions();
        assertEquals(2, selectedPages.size());
        assertEquals(className + " TestHome", selectedPages.get(0).getLabel());
        assertEquals(className + " Page 2", selectedPages.get(1).getLabel());
        assertEquals(Arrays.asList(className + ".space.pageclassfieldtesthome", className + ".pageclassfieldpage2"),
            pagePicker.getValues());

        // Clear the list of selected pages.
        pagePicker.clearSelectedSuggestions();
        assertEquals(0, pagePicker.getSelectedSuggestions().size());
        assertTrue(pagePicker.getValues().isEmpty());
    }

    @Test
    @Order(4)
    void saveAndInitialSelection(TestReference testReference)
    {
        ApplicationClassEditPage editor = goToEditor(testReference);
        String className = testReference.getSpaceReferences().get(0).getName();
        SuggestInputElement pagePicker = new SuggestClassFieldEditPane(editor.addField("Page").getName()).getPicker();
        // Make sure the picker is ready.
        pagePicker.click().waitForSuggestions();
        pagePicker.sendKeys(className).waitForSuggestions().sendKeys(Keys.ENTER);
        editor.clickSaveAndView().edit();

        SuggestClassFieldEditPane pageField = new SuggestClassFieldEditPane("page1");
        pagePicker = pageField.getPicker();
        List<SuggestionElement> selectedPages = pagePicker.getSelectedSuggestions();
        assertEquals(1, selectedPages.size());
        assertEquals(className + " Page 1", selectedPages.get(0).getLabel());
        assertEquals(Collections.singletonList(className + ".pageclassfieldpage1"), pagePicker.getValues());

        // Enable multiple selection.
        pageField.openConfigPanel();
        pageField.setMultipleSelect(true);
        pageField.closeConfigPanel();

        // Re-take the page picker because the display has been reloaded.
        pagePicker = pageField.getPicker();

        // Select one more page.
        pagePicker.click().waitForSuggestions().sendKeys(className).waitForSuggestions().sendKeys(Keys.ENTER);
        editor.clickSaveAndContinue();
        editor.clickCancel().edit();

        pagePicker = new SuggestClassFieldEditPane("page1").getPicker();
        selectedPages = pagePicker.getSelectedSuggestions();
        assertEquals(2, selectedPages.size());
        assertEquals(className + " Page 1", selectedPages.get(0).getLabel());
        assertEquals(className + " Page 2", selectedPages.get(1).getLabel());
        assertEquals(Arrays.asList(className + ".pageclassfieldpage1", className + ".pageclassfieldpage2"),
            pagePicker.getValues());

        // We should be able to input free text also.
        pagePicker.click().waitForSuggestions().sendKeys("foobar").waitForSuggestions().selectTypedText();
        editor.clickSaveAndContinue();
        editor.clickCancel().edit();

        pagePicker = new SuggestClassFieldEditPane("page1").getPicker();
        selectedPages = pagePicker.getSelectedSuggestions();
        assertEquals(3, selectedPages.size());
        assertEquals("foobar", selectedPages.get(2).getLabel());
        assertEquals(Arrays.asList(className + ".pageclassfieldpage1", className + ".pageclassfieldpage2", "foobar"),
            pagePicker.getValues());

        // Delete the fake page.
        selectedPages.get(2).delete();
        assertEquals(2, pagePicker.getSelectedSuggestions().size());

        // Delete all selected pages.
        pagePicker.clearSelectedSuggestions();
        editor.clickSaveAndContinue();
        editor.clickCancel().edit();

        pagePicker = new SuggestClassFieldEditPane("page1").getPicker();
        assertEquals(0, pagePicker.getSelectedSuggestions().size());
        assertTrue(pagePicker.getValues().isEmpty());
    }

    @Test
    @Order(5)
    void applicationEntry(TestReference testReference, TestUtils testUtils)
    {
        ApplicationClassEditPage editor = goToEditor(testReference);
        String className = testReference.getSpaceReferences().get(0).getName();
        String methodName = testReference.getLastSpaceReference().getName();

        // Create the application class.
        SuggestInputElement pagePicker = new SuggestClassFieldEditPane(editor.addField("Page").getName()).getPicker();
        // Make sure the picker is ready.
        pagePicker.click().waitForSuggestions();
        pagePicker.sendKeys(className).waitForSuggestions().sendKeys(Keys.ENTER);
        editor.clickSaveAndView();

        // Create the application entry.
        ClassSheetPage classSheetPage = new ClassSheetPage();
        // TODO: find out how to have access to the expected UI here. a Class Template object must be created
        // ça doit être possible en crééant le template, puis recliquant sur le lien qui propose d'ajouter un truc dedans
        InlinePage entryEditPage = classSheetPage.createNewDocument(className, methodName + "Entry");

        // Assert the initial value.
        String id = className + "." + methodName + "_0_page1";
        pagePicker = new SuggestInputElement(testUtils.getDriver().findElement(By.id(id)));
        List<SuggestionElement> selectedPages = pagePicker.getSelectedSuggestions();
        assertEquals(1, selectedPages.size());
        assertEquals(className + " Page 1", selectedPages.get(0).getLabel());
        assertEquals(Collections.singletonList(className + ".pageclassfieldpage1"), pagePicker.getValues());

        // Make sure the picker is ready.
        pagePicker.click().waitForSuggestions();
        pagePicker.sendKeys(className).waitForSuggestions().sendKeys(Keys.ENTER);
        // Change the selected page.
        pagePicker.clearSelectedSuggestions().sendKeys(className, " testh").waitForSuggestions().sendKeys(Keys.ENTER);
        // We wait for the page to load because Selenium doesn't do it all the time when Save & View is clicked.
        entryEditPage.clickSaveAndView().waitUntilPageIsLoaded();

        // Assert the view mode.
        List<WebElement> pages = testUtils.getDriver().findElements(By.cssSelector("#xwikicontent dd a"));
        assertEquals(1, pages.size());
        assertEquals(className + " TestHome", pages.get(0).getText());
    }
}
