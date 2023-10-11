package com.browserstack.steps;

import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browserstack.util.OsUtility;
import com.browserstack.util.Utility;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class SetupSteps {

    private final StepData stepData;
    protected String driverBaseLocation = Paths.get(System.getProperty("user.dir"), "/src/test/resources/drivers")
            .toString();

    private static final String PASSED = "passed";
    private static final String FAILED = "failed";
    private static final String URL = "https://bstackdemo.com";
    private static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String BROWSERSTACK_HUB_URL = "https://hub.browserstack.com/wd/hub";

    public static Logger log = LoggerFactory.getLogger(SetupSteps.class);

    public SetupSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @Before
    public void setUp(Scenario scenario) throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        if (StringUtils.isNoneEmpty(System.getProperty("env"))) {
            switch (System.getProperty("env")) {
                case "on-prem":
                    if (OsUtility.isWindows()) {
                        System.setProperty(WEBDRIVER_CHROME_DRIVER,
                                Paths.get(driverBaseLocation, "/chromedriver.exe").toString());
                    } else {
                        System.setProperty(WEBDRIVER_CHROME_DRIVER,
                                Paths.get(driverBaseLocation, "/chromedriver").toString());
                    }
                    stepData.webDriver = new ChromeDriver();
                    stepData.url = URL;
                    break;
                default:
                    stepData.url = "https://bstackdemo.com";
                    Utility.setLocationSpecificCapabilities(caps);
                    stepData.webDriver = new RemoteWebDriver(new URL(BROWSERSTACK_HUB_URL), caps);
                    stepData.webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
            }
        } else {
            throw new RuntimeException("Something went wrong!");
        }
    }

    @After
    public void teardown(Scenario scenario) throws Exception {
        if (StringUtils.isNoneEmpty(System.getProperty("env"))
                && System.getProperty("env").equalsIgnoreCase("remote")) {
            if (scenario.isFailed()) {
                Utility.setSessionStatus(stepData.webDriver, FAILED, String.format("%s failed.", scenario.getName()));
            } else {
                Utility.setSessionStatus(stepData.webDriver, PASSED, String.format("%s passed.", scenario.getName()));
            }
        }
        stepData.webDriver.quit();
    }

}
