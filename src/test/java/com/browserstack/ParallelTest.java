package com.browserstack;

import com.browserstack.util.Utility;
import io.cucumber.core.cli.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ParallelTest {

    public static ThreadLocal<JSONObject> threadLocalValue = new ThreadLocal<>();

    public static Logger log = LoggerFactory.getLogger(ParallelTest.class);

    public static void main(String[] args) throws IOException, ParseException {
        JSONObject testConfigs;
        JSONObject testSelectedConfig;
        JSONParser parser = new JSONParser();
        if (System.getenv("caps") != null) {
            testConfigs = (JSONObject) parser.parse(System.getenv("caps"));
        } else {
            testConfigs = (JSONObject) parser.parse(new FileReader("src/test/resources/config/caps.json"));
        }
        if (System.getProperty("caps-type") != null) {
            testSelectedConfig = (JSONObject) ((JSONObject) testConfigs.get("tests")).get(System.getProperty("caps-type"));
        } else {
            testSelectedConfig = (JSONObject) ((JSONObject) testConfigs.get("tests")).get("parallel");
        }
        JSONArray environments = (JSONArray) testSelectedConfig.get("env_caps");
        log.debug("Selected Test Config : " + testSelectedConfig.toJSONString());
        for (Object obj : environments) {
            JSONObject singleConfig = Utility.getCombinedCapability((Map<String, String>) obj, testConfigs, testSelectedConfig);
            log.debug("Single Test Config : " + singleConfig.toJSONString());
            Thread thread = new Thread(() -> {
                System.setProperty("parallel", "true");
                threadLocalValue.set(singleConfig);
                try {
                    String[] argv = new String[]{"-g", "", "src/test/resources/com/browserstack"};
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    Main.run(argv, contextClassLoader);
                } catch (Exception e) {
                    e.getStackTrace();
                } finally {
                    threadLocalValue.remove();
                }
            });
            thread.start();
        }
    }

}
