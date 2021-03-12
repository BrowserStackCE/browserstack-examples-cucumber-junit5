package com.browserstack.steps;

import io.cucumber.java.en.And;
import org.openqa.selenium.By;

public class CheckoutPageSteps {

    private final StepData stepData;

    public CheckoutPageSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @And("I type {string} in Post Code")
    public void iTypeInPostCode(String postCode) {
        stepData.webDriver.findElement(By.cssSelector(".dynamic-form-field--postCode #provinceInput")).sendKeys(postCode);
    }

    @And("I click on Checkout Button")
    public void iClickOnCheckoutButton() throws InterruptedException {
        stepData.webDriver.findElement(By.id("checkout-shipping-continue")).click();
        stepData.webDriver.findElement(By.cssSelector(".button")).click();
    }

    @And("I enter shipping details {string}, {string}, {string}, {string} and {string}")
    public void iEnterShippingDetailsAnd(String first, String last, String address, String province, String postCode) {
        stepData.webDriver.findElement(By.id("firstNameInput")).sendKeys(first);
        stepData.webDriver.findElement(By.id("lastNameInput")).sendKeys(last);
        stepData.webDriver.findElement(By.id("addressLine1Input")).sendKeys(address);
        stepData.webDriver.findElement(By.id("provinceInput")).sendKeys(province);
        stepData.webDriver.findElement(By.id("postCodeInput")).sendKeys(postCode);
    }
}
