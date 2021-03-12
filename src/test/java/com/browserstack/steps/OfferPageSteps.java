package com.browserstack.steps;

import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class OfferPageSteps {

    private final StepData stepData;

    public OfferPageSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @Then("I should see Offer elements")
    public void iShouldSeeOfferElements() {
        try {
            WebElement element = stepData.webDriver.findElement(By.cssSelector(".pt-6"));
            Assertions.assertNotNull(element);
        } catch (NoSuchElementException e) {
            throw new AssertionError("There are no offers");
        }
    }

}
