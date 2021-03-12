package com.browserstack.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class CommonSteps {

    private final StepData stepData;

    public CommonSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @Given("I navigate to website")
    public void iNavigateToWebsite() {
        stepData.webDriver.get(stepData.url);
    }

    @And("I click on {string} link")
    public void iClickOnLink(String linkText) throws InterruptedException {
        stepData.webDriver.findElement(By.linkText(linkText)).click();
    }

    @And("I type {string} in {string}")
    public void iTypeIn(String text, String inputName) throws InterruptedException {
        if(inputName.equalsIgnoreCase("username")){
            stepData.webDriver.findElement(By.xpath("//*[@id=\"username\"]/div/div[1]")).click();
            stepData.webDriver.findElement(By.id("react-select-2-input")).sendKeys(text);
            stepData.webDriver.findElement(By.id("react-select-2-input")).sendKeys(Keys.ENTER);
        } else if (inputName.equalsIgnoreCase("password")) {
            stepData.webDriver.findElement(By.xpath("//*[@id=\"password\"]/div/div[1]")).click();
            stepData.webDriver.findElement(By.id("react-select-3-input")).sendKeys(text);
            stepData.webDriver.findElement(By.id("react-select-3-input")).sendKeys(Keys.ENTER);
        }
    }

}
