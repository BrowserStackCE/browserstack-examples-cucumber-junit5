package com.browserstack.util;

import org.json.simple.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utility {

    private Utility() {
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

}
