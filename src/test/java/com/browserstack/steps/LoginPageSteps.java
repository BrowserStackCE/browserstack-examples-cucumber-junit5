package com.browserstack.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;

public class LoginPageSteps {

    private final StepData stepData;

    public LoginPageSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @And("I press Log In Button")
    public void iPressLogin() throws InterruptedException {
        stepData.webDriver.findElement(By.cssSelector(".Button_root__24MxS")).click();
    }

    @Then("I should see {string} as Login Error Message")
    public void iShouldSeeAsLoginErrorMessage(String expectedMessage) {
        try {
            String errorMessage = stepData.webDriver.findElement(By.cssSelector(".api-error")).getText();
            Assertions.assertEquals(expectedMessage, errorMessage);
        } catch (NoSuchElementException e) {
            throw new AssertionError("Error in logging in");
        }
    }

    @And("I SignIn as {string} with {string} password")
    public void iSignInAsWithPassword(String username, String password) {
        stepData.webDriver.findElement(By.linkText("Sign In")).click();
        stepData.webDriver.findElement(By.xpath("//*[@id=\"username\"]/div/div[1]")).click();
        stepData.webDriver.findElement(By.id("react-select-2-input")).sendKeys(username);
        stepData.webDriver.findElement(By.id("react-select-2-input")).sendKeys(Keys.ENTER);
        stepData.webDriver.findElement(By.xpath("//*[@id=\"password\"]/div/div[1]")).click();
        stepData.webDriver.findElement(By.id("react-select-3-input")).sendKeys(password);
        stepData.webDriver.findElement(By.id("react-select-3-input")).sendKeys(Keys.ENTER);
        stepData.webDriver.findElement(By.cssSelector(".Button_root__24MxS")).click();
    }
}
