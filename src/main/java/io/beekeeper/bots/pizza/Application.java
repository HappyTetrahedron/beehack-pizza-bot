package io.beekeeper.bots.pizza;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Application {
    private final static String ENV_KEY_BKPR_URL = "BEEKEEPER_API_URL";
    private final static String ENV_KEY_BKPR_KEY = "BEEKEEPER_ACCESS_KEY";

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        Map<String, String> env = System.getenv();
        final String beekeeperURL = getVarOrExit(env, ENV_KEY_BKPR_URL);
        final String beekeeperKey = getVarOrExit(env, ENV_KEY_BKPR_KEY);

    }

    private static String getVarOrExit(Map<String, String> env, String key) {
        String val = env.get(key);
        if (val == null || val.isEmpty()) {
            throw new RuntimeException(String.format("'%s' needs to be in the environment", key));
        } else {
            return val;
        }
    }
}
