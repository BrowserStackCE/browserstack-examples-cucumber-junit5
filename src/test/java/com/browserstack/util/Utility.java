package com.browserstack.util;

import org.json.simple.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utility {

    private Utility() {
    }


    private static final String LOCATION_SCRIPT_FORMAT = "navigator.geolocation.getCurrentPosition = function(success){\n" +
            "    var position = { \"coords\":{\"latitude\":\"%s\",\"longitude\":\"%s\"}};\n" +
            "    success(position);\n" +
            "}";
    private static final String OFFER_LATITUDE = "19";
    private static final String OFFER_LONGITUDE = "72";

    private static Object SYNCHRONIZER = new Object();
    private static String epochTime = null;

    public static String getEpochTime() {
        if (epochTime == null) {
            synchronized (SYNCHRONIZER) {
                if (epochTime == null) {
                    epochTime = String.valueOf(Instant.now().toEpochMilli());
                }
            }
        }
        return epochTime;
    }

    public static Map<String, String> getLocalOptions(JSONObject config) {
        Map<String, String> localOptions = new HashMap<>();
        JSONObject localOptionsJson = (JSONObject) ((JSONObject) ((JSONObject) config.get("tests")).get(System.getProperty("caps-type"))).get("local_binding_caps");
        for (Object o : localOptionsJson.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            if (localOptions.get(pair.getKey().toString()) == null) {
                localOptions.put(pair.getKey().toString(), pair.getValue().toString());
            }
        }
        return localOptions;
    }

    public static void setSessionStatus(WebDriver webDriver, String status, String reason) {
        JavascriptExecutor jse = (JavascriptExecutor) webDriver;
        jse.executeScript(String.format("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"%s\", \"reason\": \"%s\"}}", status, reason));
    }

    public static JSONObject getCombinedCapability(Map<String, String> envCapabilities, JSONObject config, JSONObject caps) {
        JSONObject capabilities = new JSONObject();
        Iterator it = envCapabilities.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            capabilities.put(pair.getKey().toString(), pair.getValue().toString());
        }
        Map<String, String> commonCapabilities = (Map<String, String>) caps.get("common_caps");
        it = commonCapabilities.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (capabilities.get(pair.getKey().toString()) == null) {
                capabilities.put(pair.getKey().toString(), pair.getValue().toString());
            }
        }
        JSONObject singleConfig = new JSONObject();
        singleConfig.put("user", config.get("user"));
        singleConfig.put("key", config.get("key"));
        singleConfig.put("capabilities", capabilities);
        if (caps.containsKey("application_endpoint")) {
            singleConfig.put("application_endpoint", caps.get("application_endpoint"));
        } else {
            singleConfig.put("application_endpoint", config.get("application_endpoint"));
        }
        return singleConfig;
    }

    public static boolean isAscendingOrder(List<WebElement> priceWebElement, int length) {
        if (priceWebElement == null || length < 2)
            return true;
        if (Integer.parseInt(priceWebElement.get(length - 2).getText()) > Integer.parseInt(priceWebElement.get(length - 1).getText()))
            return false;
        return isAscendingOrder(priceWebElement, length - 1);
    }

    public static void setLocationSpecificCapabilities(DesiredCapabilities desiredCapabilities) {
        String browser = (String) desiredCapabilities.getCapability("browser");
        if (browser != null) {
            if (browser.equalsIgnoreCase("Chrome")) {
                desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, getChromeOptions());
            } else if (browser.equalsIgnoreCase("Firefox")) {
                desiredCapabilities.setCapability(FirefoxDriver.PROFILE, getFirefoxProfile());
            }
        }
    }

    public static void mockGPS(WebDriver webDriver) {
        String locationScript = String.format(LOCATION_SCRIPT_FORMAT, OFFER_LATITUDE, OFFER_LONGITUDE);
        ((JavascriptExecutor) webDriver).executeScript(locationScript);
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        Map<String, Object> profile = new HashMap<>();
        Map<String, Object> contentSettings = new HashMap<>();
        contentSettings.put("geolocation", 1);
        profile.put("managed_default_content_settings", contentSettings);
        prefs.put("profile", profile);
        options.setExperimentalOption("prefs", prefs);
        return options;
    }

    private static FirefoxProfile getFirefoxProfile() {
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("geo.enabled", false);
        firefoxProfile.setPreference("geo.provider.use_corelocation", false);
        firefoxProfile.setPreference("geo.prompt.testing", false);
        firefoxProfile.setPreference("geo.prompt.testing.allow", false);
        return firefoxProfile;
    }

}
