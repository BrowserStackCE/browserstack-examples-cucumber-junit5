package com.browserstack;

import com.browserstack.util.Utility;
import io.cucumber.core.cli.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ParallelTest {

    public static ThreadLocal<JSONObject> threadLocalValue = new ThreadLocal<>();

    public static void main(String[] args) throws IOException, ParseException {
        JSONObject config;
        JSONObject caps;
        JSONParser parser = new JSONParser();
        if(System.getenv("caps")!= null) {
            config = (JSONObject) parser.parse(System.getenv("caps"));
        } else {
            config = (JSONObject) parser.parse(new FileReader("src/test/resources/config/caps.json"));
        }
        if(System.getProperty("caps-type") != null) {
            caps = (JSONObject) ((JSONObject) config.get("tests")).get(System.getProperty("caps-type"));
        } else {
            caps = (JSONObject) ((JSONObject) config.get("tests")).get("parallel");
        }
        JSONArray environments = (JSONArray)caps.get("env_caps");
        System.out.println(caps.toJSONString());
        for (Object obj: environments) {
            JSONObject singleConfig = Utility.getCombinedCapability((Map<String, String>) obj,config,caps);
            System.out.println(singleConfig.toJSONString());
            Thread thread = new Thread(() -> {
                System.setProperty("parallel","true");
                threadLocalValue.set(singleConfig);
                try {
                    String[] argv = new String[]{"-g", "", "src/test/resources/com/com.browserstack"};
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    Main.run(argv, contextClassLoader);
                } catch(Exception e) {
                    e.getStackTrace();
                } finally {
                    threadLocalValue.remove();
                }
            });
            thread.start();
        }
    }

}
