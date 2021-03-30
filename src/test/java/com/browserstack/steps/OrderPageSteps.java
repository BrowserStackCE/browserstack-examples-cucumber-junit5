package com.browserstack.steps;

import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class OrderPageSteps {

    private final StepData stepData;

    public OrderPageSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @Then("I should see elements in list")
    public void iShouldSeeElementsInList() {
        WebDriverWait wait = new WebDriverWait(stepData.webDriver, 5);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#logout")));
        WebElement element = null;
        try {
            element = stepData.webDriver.findElement(By.cssSelector("#__next > main > div > div"));
            List<WebElement> orders = element.findElements(By.tagName("div"));
            Assertions.assertNotEquals(0, orders.size());
        } catch (NoSuchElementException e) {
            throw new AssertionError("There are no orders");
        }
    }

}
