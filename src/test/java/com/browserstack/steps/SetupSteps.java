package com.browserstack.steps;

import com.browserstack.ParallelTest;
import com.browserstack.local.Local;
import com.browserstack.util.OsUtility;
import com.browserstack.util.Utility;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SetupSteps {

    private final StepData stepData;
    private Local bstackLocal;
    protected String driverBaseLocation = Path.of(System.getProperty("user.dir"), "/src/test/resources/drivers").toString();

    private static final String PASSED = "passed";
    private static final String FAILED = "failed";
    private static final String URL = "https://bstackdemo.com";
    private static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String DOCKER_SELENIUM_HUB_URL = "http://localhost:4444/wd/hub";
    private static final String BROWSERSTACK_HUB_URL = "https://hub.browserstack.com/wd/hub";
    private static final String CAPABILITY_CONFIG_FILE = "src/test/resources/config/caps.json";
    private static final String REPO_NAME = "browserstack-examples-cucumber-junit5 - ";

    public static Logger log = LoggerFactory.getLogger(ParallelTest.class);

    public SetupSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @Before
    public void setUp(Scenario scenario) throws Exception {
        JSONObject testConfigs;
        JSONObject testSelectedConfig;
        JSONParser parser = new JSONParser();
        DesiredCapabilities caps = new DesiredCapabilities();
        if (StringUtils.isNoneEmpty(System.getProperty("env"))) {
            switch (System.getProperty("env")) {
                case "on-prem":
                    if (OsUtility.isWindows()) {
                        System.setProperty(WEBDRIVER_CHROME_DRIVER, Path.of(driverBaseLocation, "/chromedriver.exe").toString());
                    } else {
                        System.setProperty(WEBDRIVER_CHROME_DRIVER, Path.of(driverBaseLocation, "/chromedriver").toString());
                    }
                    stepData.webDriver = new ChromeDriver();
                    stepData.url = URL;
                    break;
                case "docker":
                    DesiredCapabilities dockerCaps = new DesiredCapabilities(new ChromeOptions());
                    Utility.setLocationSpecificCapabilities(dockerCaps);
                    stepData.webDriver = new RemoteWebDriver(new URL(DOCKER_SELENIUM_HUB_URL), dockerCaps);
                    stepData.url = URL;
                    break;
                default:
                    if (System.getenv("caps") != null) {
                        testConfigs = (JSONObject) parser.parse(System.getenv("caps"));
                    } else {
                        testConfigs = (JSONObject) parser.parse(new FileReader(CAPABILITY_CONFIG_FILE));
                    }
                    if (System.getProperty("parallel") != null) {
                        testSelectedConfig = ParallelTest.threadLocalValue.get();
                        log.debug("Test Config : " + testSelectedConfig.toJSONString());
                    } else {
                        JSONObject singleCapabilityJson = (JSONObject) ((JSONObject) testConfigs.get("tests")).get(System.getProperty("caps-type"));
                        JSONArray environments = (JSONArray) singleCapabilityJson.get("env_caps");
                        testSelectedConfig = Utility.getCombinedCapability((Map<String, String>) environments.get(0), testConfigs, singleCapabilityJson);
                    }

                    Map<String, String> commonCapabilities = (Map<String, String>) testSelectedConfig.get("capabilities");
                    Iterator it = commonCapabilities.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        if (caps.getCapability(pair.getKey().toString()) == null) {
                            caps.setCapability(pair.getKey().toString(), pair.getValue().toString());
                        }
                    }
                    setBuildAndTestName(caps, scenario);
                    String username = System.getenv("BROWSERSTACK_USERNAME");
                    if (username == null) {
                        username = (String) testSelectedConfig.get("user");
                    }
                    String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
                    if (accessKey == null) {
                        accessKey = (String) testSelectedConfig.get("key");
                    }
                    stepData.url = (String) testSelectedConfig.get("application_endpoint");
                    caps.setCapability("browserstack.user", username);
                    caps.setCapability("browserstack.key", accessKey);
                    setupLocal(caps, testConfigs, accessKey);
                    Utility.setLocationSpecificCapabilities(caps);
                    stepData.webDriver = new RemoteWebDriver(new URL(BROWSERSTACK_HUB_URL), caps);
                    stepData.webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
                    if (!caps.getCapabilityNames().contains("device")) {
                        stepData.webDriver.manage().window().maximize();
                    }
            }
        } else {
            throw new RuntimeException("Please set 'env' property as 'on-prem', 'docker' or remote.");
        }
    }

    private void setupLocal(DesiredCapabilities caps, JSONObject testConfigs, String accessKey) throws Exception {
        if (caps.getCapability("browserstack.local") != null && caps.getCapability("browserstack.local").equals("true")) {
            String localIdentifier = RandomStringUtils.randomAlphabetic(8);
            caps.setCapability("browserstack.localIdentifier", localIdentifier);
            bstackLocal = new Local();
            Map<String, String> options = Utility.getLocalOptions(testConfigs);
            options.put("key", accessKey);
            options.put("localIdentifier", localIdentifier);
            bstackLocal.start(options);
        }
    }

    private void setBuildAndTestName(DesiredCapabilities caps, Scenario scenario) {
        caps.setCapability("name", scenario.getName());
        String buildName = System.getenv("BROWSERSTACK_BUILD_NAME");
        if (buildName == null) {
            buildName = Utility.getEpochTime();
        }
        caps.setCapability("build", REPO_NAME + buildName);
    }

    @After
    public void teardown(Scenario scenario) throws Exception {
        if (StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("remote")) {
            if (scenario.isFailed()) {
                Utility.setSessionStatus(stepData.webDriver, FAILED, String.format("%s failed.", scenario.getName()));
            } else {
                Utility.setSessionStatus(stepData.webDriver, PASSED, String.format("%s passed.", scenario.getName()));
            }
        }
        stepData.webDriver.quit();
        if (bstackLocal != null) bstackLocal.stop();
    }

}
