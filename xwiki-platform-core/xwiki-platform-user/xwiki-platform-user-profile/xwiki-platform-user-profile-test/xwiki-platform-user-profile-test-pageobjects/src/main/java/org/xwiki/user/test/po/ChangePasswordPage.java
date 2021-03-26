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
package org.xwiki.user.test.po;

import java.util.function.BooleanSupplier;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.BasePage;

/**
 * User profile, change password action.
 */
public class ChangePasswordPage extends BasePage
{
    private static final String ERROR_MESSAGE_SELECTOR = "span.box.errormessage";

    private static final String VALIDATION_ERROR_MESSAGE_SELECTOR = "span.LV_validation_message.LV_invalid";

    private static final String SUCCESS_MESSAGE_SELECTOR = "span.box.infomessage";

    @FindBy(xpath = "//input[@id='xwikioriginalpassword']")
    private WebElement originalPassword;

    @FindBy(xpath = "//input[@id='xwikipassword']")
    private WebElement password1;

    @FindBy(xpath = "//input[@id='xwikipassword2']")
    private WebElement password2;

    @FindBy(xpath = "//input[@value='Save']")
    private WebElement changePassword;

    @FindBy(css = "a.secondary.button")
    private WebElement cancelPasswordChange;

    @FindBy(css = ERROR_MESSAGE_SELECTOR)
    private WebElement errorMessage;

    @FindBy(css = VALIDATION_ERROR_MESSAGE_SELECTOR)
    private WebElement validationErrorMessage;

    @FindBy(css = SUCCESS_MESSAGE_SELECTOR)
    private WebElement successMessage;

    /**
     * Fill the change password form with the original password, the new password and the confirmation of the new
     * password.
     *
     * @param originalPassword the original password
     * @param password the new password
     * @param password2 the confirmation of the new password
     * @see #changePasswordAsAdmin(String, String)
     */
    public void changePassword(String originalPassword, String password, String password2)
    {
        this.originalPassword.clear();
        this.originalPassword.sendKeys(originalPassword);
        this.password1.clear();
        this.password1.sendKeys(password);
        this.password2.clear();
        this.password2.sendKeys(password2);
    }

    /**
     * Fill the change password form when the current user is an Admin.
     *
     * @param password the new password
     * @param password2 the confirmation of the new password
     * @see #changePassword(String, String, String)
     */
    public void changePasswordAsAdmin(String password, String password2)
    {
        this.password1.clear();
        this.password1.sendKeys(password);
        this.password2.clear();
        this.password2.sendKeys(password2);
    }

    /**
     * @return the text of the change password form error message
     */
    public String getErrorMessage()
    {
        return this.errorMessage.getText();
    }

    /**
     * @return the text of the change password form validation error message
     */
    public String getValidationErrorMessage()
    {
        return this.validationErrorMessage.getText();
    }

    /**
     * @return the text of the change password form success message
     */
    public String getSuccessMessage()
    {
        return this.successMessage.getText();
    }

    /**
     * Submit the change password form and wait for at least one success or error message to be displayed before
     * continuing. If you wish to assert a form error message after submitting, use {@link #submit(BooleanSupplier)} and
     * define a condition with the error message you except to see displayed.
     *
     * @see #submit(BooleanSupplier)
     */
    public void submit()
    {
        // We cannot wait on a page reload because of the live error messages,
        // so we wait on the various kind of messages we can have, to avoid getting
        // StaleElementReference afterwards.
        submit(() -> isValidationErrorMessageDisplayed() || isErrorMessageDisplayed() || isSuccessMessageDisplayed());
    }

    /**
     * Submit the change password form and wait for the provided continuation condition to be true. The continuation
     * conditions are often a boolean expression combining {@link #isSuccessMessageDisplayed()}, {@link
     * #isErrorMessageDisplayed()}, and {@link #isValidationErrorMessageDisplayed()}.
     *
     * @param condition the continuation condition
     * @since 13.2
     * @since 12.10.6
     */
    public void submit(BooleanSupplier condition)
    {
        this.changePassword.click();

        getDriver().waitUntilCondition(input -> condition.getAsBoolean());
    }

    /**
     * @return {@code true} if a success message is displayed, {@code false} otherwise
     * @since 13.2
     * @since 12.10.6
     */
    public boolean isSuccessMessageDisplayed()
    {
        return isDisplayed(By.cssSelector(SUCCESS_MESSAGE_SELECTOR));
    }

    /**
     * @return {@code true} if an error message message is displayed, {@code false} otherwise
     * @since 13.2
     * @since 12.10.6
     */
    public boolean isErrorMessageDisplayed()
    {
        return isDisplayed(By.cssSelector(ERROR_MESSAGE_SELECTOR));
    }

    /**
     * @return {@code true} if a validation error message message is displayed, {@code false} otherwise
     * @since 13.2
     * @since 12.10.6
     */
    public boolean isValidationErrorMessageDisplayed()
    {
        return isDisplayed(By.cssSelector(VALIDATION_ERROR_MESSAGE_SELECTOR));
    }

    /**
     * Inspects the target and returns {@code true} if it is displayed. This method is safe regarding staled targets,
     * and the returned value is {@code false} in this case.
     * <p>
     * Note that the target is inspected without waiting and {@code false} is returned instantaneously if the target is
     * not found.
     *
     * @param target the target
     * @return {@code true} if the target is displayed, {@code false} otherwise
     */
    private boolean isDisplayed(By target)
    {
        boolean liveErrorIsDisplayed = false;
        try {
            // Fails fast with findElementWithoutWaiting when the targeted element is staled,
            // because isDisplayed fails too but much more slowly, increasing the chance to see 
            // waitUntilCondition timeout even if the expected message is finally displayed. 
            WebElement elementWithoutWaiting = getDriver().findElementWithoutWaiting(target);
            liveErrorIsDisplayed = elementWithoutWaiting.isDisplayed();
        } catch (StaleElementReferenceException | NoSuchElementException e) {
        }
        return liveErrorIsDisplayed;
    }

    /**
     * Click on the cancel button of the change password form.
     */
    public void cancel()
    {
        this.cancelPasswordChange.click();
    }
}
