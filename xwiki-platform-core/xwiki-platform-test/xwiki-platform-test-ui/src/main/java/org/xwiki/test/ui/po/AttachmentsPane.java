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
package org.xwiki.test.ui.po;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Represents the actions possible on the Attachment Pane at the bottom of a page.
 *
 * @version $Id$
 * @since 3.2M3
 */
public class AttachmentsPane extends BaseElement
{
    private static final String DELETE_MODAL_ID = "deleteAttachment";

    @FindBy(id = "Attachmentspane")
    private WebElement pane;

    @FindBy(xpath = "//input[@value='Add another file']")
    private WebElement addAnotherFile;

    @FindBy(css = "#Attachmentspane input[name='shouldSkipRecycleBin'][value='false']")
    private WebElement optionToRecycleBin;

    @FindBy(css = "#Attachmentspane input[name='shouldSkipRecycleBin'][value='true']")
    private WebElement optionSkipRecycleBin;

    public boolean isOpened()
    {
        return getDriver().findElementWithoutWaiting(By.id("attachmentscontent")).isDisplayed();
    }

    /**
     * Fills the URL with the specified file path.
     *
     * @param filePath the path to the file to upload in URL form (the file *must* exist in the target directory).
     */
    public void setFileToUpload(final String filePath)
    {
        final List<WebElement> inputs = this.pane.findElements(By.className("uploadFileInput"));
        inputs.get(inputs.size() - 1).sendKeys(filePath);
    }

    public void waitForUploadToFinish(String fileName)
    {
        waitForNotificationSuccessMessage("Attachment uploaded: " + fileName);
    }

    public void clickHideProgress()
    {
        this.pane.findElement(By.xpath("//a[text()='Hide upload status']")).click();
    }

    /**
     * Adds another input field for attaching a file.
     */
    public void addAnotherFile()
    {
        this.addAnotherFile.click();
    }

    public void clickAttachFiles()
    {
        this.pane
            .findElement(By.xpath(
                "//form[@id='AddAttachment']//input[@class='button' and @type='submit' and " + "@value='Attach']"))
            .click();
    }

    /**
     * @since 3.2M3
     */
    public WebElement getAttachmentLink(String attachmentName)
    {
        return getDriver().findElement(
            By.xpath("//div[@id='_attachments']//a[@title = 'Download this attachment' and contains(@href, '"
                         + attachmentName + "')]"));
    }

    /**
     * Deletes the corresponding file name.
     *
     * @param attachmentName the name of the attachment to be deleted
     */
    public void deleteAttachmentByFileByName(String attachmentName)
    {
        deleteAttachmentByFileByName(attachmentName, null);
    }

    /**
     * Deletes the corresponding file name.
     *
     * @param attachmentName the name of the attachment to be deleted
     * @param shouldSkipTheRecycleBin if {@code null} the parameter is ignored, if {@code false} the option to send the
     *                                attachment to the recycle bin is selected, if {code true} the option to skip the
     *                                recycle bin is selected.
     */
    public void deleteAttachmentByFileByName(String attachmentName, Boolean shouldSkipTheRecycleBin)
    {
        // We initialize before so we can remove the animation before the modal is shown
        ConfirmationModal confirmDelete = new ConfirmationModal(By.id("deleteAttachment"));
        openDeleteAttachmentModal(attachmentName);
        if (shouldSkipTheRecycleBin != null) {
            if (shouldSkipTheRecycleBin) {
                this.optionSkipRecycleBin.click();
            } else {
                this.optionToRecycleBin.click();
            }
        }
        confirmDelete.clickOk();
        getDriver().waitUntilElementDisappears(
            By.xpath("//div[@id='attachmentscontent']//a[text()='" + attachmentName + "']"));
        getDriver().waitUntilElementIsVisible(By.xpath("//div[@id='Attachmentspane']"));
    }

    /**
     * Opens the delete attachment modal screen.
     * @param attachmentName the name of the attachment to be deleted
     */
    public void openDeleteAttachmentModal(String attachmentName)
    {
        String xpathExpression = "//div[@id='attachmentscontent']//a[text()='" + attachmentName
                                     + "']/../../div[contains(@class, 'xwikibuttonlinks')]/a[contains(@class,'deletelink')]";
        getDriver().findElement(By.xpath(xpathExpression)).click();
    }

    /**
     * Deletes the first attachment.
     */
    public void deleteFirstAttachment()
    {
        String tmp = getDriver()
                         .findElement(
                             By.xpath("//div[@id='_attachments']/*[1]/div[@class='information']/span[@class='name']"))
                         .getText();
        getDriver()
            .findElement(By
                .xpath("//div[@id='attachmentscontent']//a[text()='" + tmp + "']/../../span[2]/a[@class='deletelink']"))
            .click();

        getDriver().findElement(By.xpath("//*[@class='xdialog-modal-container']//input[@value='Yes']")).click();
        getDriver().waitUntilElementDisappears(
            By.xpath("//*[@class='xdialog-modal-container']/*[contains(@class, 'xdialog-box-confirmation')]"));
        getDriver().waitUntilElementDisappears(
            By.xpath("//div[@id='attachmentscontent']//a[text()='" + tmp + "']/../../span[2]/a[@class='deletelink']"));
    }

    /**
     * @return the number of attachments in this document.
     */
    public int getNumberOfAttachments()
    {
        By countLocator = By.cssSelector("#Attachmentstab .itemCount");
        return Integer.parseInt(getDriver().findElement(countLocator).getText().replaceAll("[()]", ""));
    }

    /**
     * Deletes ALL the attached files
     */
    public void deleteAllAttachments()
    {
        while (this.getNumberOfAttachments() > 0) {
            this.deleteFirstAttachment();
        }
    }

    public String getUploaderOfAttachment(String attachmentName)
    {
        return getDriver().findElement(By.xpath("//div[@id='attachmentscontent']//a[text()='" + attachmentName
            + "']/../../div[@class='meta']/span[@class='publisher']/span[@class='wikilink']")).getText();
    }

    public String getLatestVersionOfAttachment(String attachmentName)
    {
        return getDriver()
            .findElement(By.xpath(
                "//div[@id='attachmentscontent']//a[text()= '" + attachmentName + "']/../../span[@class='version']/a"))
            .getText();
    }

    public String getSizeOfAttachment(String attachmentName)
    {
        return getDriver().findElement(By.xpath("//div[@id='attachmentscontent']//a[text()='" + attachmentName
            + "']/../../div[@class='meta']/span[@class='size']")).getText().replaceAll("[()]", "");

    }

    public String getDateOfLastUpload(String attachmentName)
    {
        return getDriver().findElement(By.xpath("//div[@id='attachmentscontent']//a[text()='" + attachmentName
            + "']/../../div[@class='meta']/span[@class='date']")).getText().replaceFirst("on", "");
    }

    public boolean attachmentExistsByFileName(String attachmentName)
    {
        try {
            getDriver()
                .findElement(By.xpath("//a[@title = 'Download this attachment' and . = '" + attachmentName + "']"));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    /**
     * @return {@code true} if the form proposing to skip the recycle bin is displayed, {@code false} otherwise
     */
    public boolean isRecycleBinOptionsDisplayed()
    {
        try {
            return this.optionSkipRecycleBin.isDisplayed() && this.optionSkipRecycleBin.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void closeDeleteAttachmentModal()
    {
        new ConfirmationModal(By.id(DELETE_MODAL_ID)).clickCancel();
    }
}
