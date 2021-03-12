package com.browserstack.steps;

import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class OrderPageSteps {

    private final StepData stepData;

    public OrderPageSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @Then("I should see elements in list")
    public void iShouldSeeElementsInList() {
        WebElement element = null;
        try {
            element = stepData.webDriver.findElement(By.cssSelector("#__next > main > div > div > h2"));
            throw new AssertionError("There are no orders");
        } catch (NoSuchElementException e) {
            Assertions.assertNull(element);
        }
    }

}
