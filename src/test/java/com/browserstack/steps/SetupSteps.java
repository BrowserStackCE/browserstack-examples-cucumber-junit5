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
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SetupSteps {

    private final StepData stepData;
    private Local local;
    protected String chromeDriverBaseLocation = Path.of(System.getProperty("user.dir"), "/src/test/resources/chromeDriver").toString();

    private static final String PASSED = "passed";
    private static final String FAILED = "failed";
    private static final String URL = "https://bstackdemo.com";
    private static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";

    public SetupSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @Before
    public void setUp(Scenario scenario) throws Exception {
        JSONObject config;
        JSONObject capabilityObject;
        JSONParser parser = new JSONParser();
        DesiredCapabilities caps = new DesiredCapabilities();
        if (StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("on-prem")) {
            if (OsUtility.isMac()) {
                System.setProperty(WEBDRIVER_CHROME_DRIVER, Path.of(chromeDriverBaseLocation, "/chromedriver").toString());
            }
            if (OsUtility.isWindows()) {
                System.setProperty(WEBDRIVER_CHROME_DRIVER, Path.of(chromeDriverBaseLocation, "/chromedriver.exe").toString());
            }
            if (OsUtility.isUnix()) {
                System.setProperty(WEBDRIVER_CHROME_DRIVER, Path.of(chromeDriverBaseLocation, "/chromedriver").toString());
            }
            stepData.webDriver = new ChromeDriver();
            stepData.url = URL;
        } else if (StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("docker")) {
            caps.setBrowserName("chrome");
            caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            stepData.webDriver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), caps);
            stepData.url = URL;
        } else {
            if(System.getenv("caps")!= null) {
                config = (JSONObject) parser.parse(System.getenv("caps"));
            } else {
                config = (JSONObject) parser.parse(new FileReader("src/test/resources/config/caps.json"));
            }
            if (System.getProperty("parallel") != null) {
                capabilityObject = ParallelTest.threadLocalValue.get();
            } else {
                JSONObject singleCapabilityJson = (JSONObject) ((JSONObject) config.get("tests")).get("single");
                JSONArray environments = (JSONArray)singleCapabilityJson.get("env_caps");
                capabilityObject = Utility.getCombinedCapability((Map<String, String>) environments.get(0),config,singleCapabilityJson);
            }

            Map<String, String> commonCapabilities = (Map<String, String>) capabilityObject.get("capabilities");
            Iterator it = commonCapabilities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (caps.getCapability(pair.getKey().toString()) == null) {
                    caps.setCapability(pair.getKey().toString(), pair.getValue().toString());
                }
            }
            caps.setCapability("name", scenario.getName());

            String username = System.getenv("BROWSERSTACK_USERNAME");
            if (username == null) {
                username = (String) capabilityObject.get("user");
            }
            String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
            if (accessKey == null) {
                accessKey = (String) capabilityObject.get("key");
            }
            stepData.url = (String) capabilityObject.get("application_endpoint");
            if (caps.getCapability("browserstack.local") != null && caps.getCapability("browserstack.local").equals("true")) {
                String localIdentifier = RandomStringUtils.randomAlphabetic(8);
                caps.setCapability("browserstack.localIdentifier", localIdentifier);
                local = new Local();
                Map<String, String> options = Utility.getLocalOptions(config);
                options.put("key", accessKey);
                options.put("localIdentifier", localIdentifier);
                local.start(options);
            }
            System.out.println(caps.toString());
            String URL = String.format("https://%s:%s@hub.browserstack.com/wd/hub", username, accessKey);
            stepData.webDriver = new RemoteWebDriver(new URL(URL), caps);
            stepData.webDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        }
    }

    @After
    public void teardown(Scenario scenario) throws Exception {
        if(StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("remote")) {
            if (scenario.isFailed()) {
                Utility.setSessionStatus(stepData.webDriver, FAILED, String.format("%s failed.", scenario.getName()));
            } else {
                Utility.setSessionStatus(stepData.webDriver, PASSED, String.format("%s passed.", scenario.getName()));
            }
        }
        stepData.webDriver.quit();
        if(local!=null) local.stop();
    }

}
