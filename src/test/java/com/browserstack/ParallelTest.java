package com.browserstack;

import com.browserstack.util.Utility;
import io.cucumber.core.cli.Main;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelTest {

    public static ThreadLocal<JSONObject> threadLocalValue = new ThreadLocal<>();

    public static void main(String[] args) throws IOException, ParseException {
        JSONObject testConfigs;
        JSONObject testSelectedConfig;
        int threadCount = 5;
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
        if (StringUtils.isNoneEmpty(System.getProperty("parallel-count")) && StringUtils.isNumeric(System.getProperty("parallel-count"))) {
            threadCount = Integer.parseInt(System.getProperty("parallel-count"));
        }
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (Object obj : environments) {
            JSONObject singleConfig = Utility.getCombinedCapability((Map<String, String>) obj, testConfigs, testSelectedConfig);
            Runnable task = new Task(singleConfig, threadLocalValue);
            pool.execute(task);
        }
        pool.shutdown();
    }

}

class Task implements Runnable {
    private JSONObject singleConfig;
    private ThreadLocal<JSONObject> threadLocalValue;

    public static Logger log = LoggerFactory.getLogger(ParallelTest.class);

    public Task(JSONObject singleConfig, ThreadLocal<JSONObject> threadLocalValue) {
        this.singleConfig = singleConfig;
        this.threadLocalValue = threadLocalValue;
    }

    public void run() {
        System.setProperty("parallel", "true");
        threadLocalValue.set(singleConfig);
        try {
            String[] argv = new String[]{"-g", "", "src/test/resources/com/browserstack"};
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Main.run(argv, contextClassLoader);
        } catch (Exception e) {
            log.error("Error with parallel test", e);
        } finally {
            threadLocalValue.remove();
        }
    }
}
