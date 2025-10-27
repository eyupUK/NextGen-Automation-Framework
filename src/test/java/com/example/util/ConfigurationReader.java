package com.example.util;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * reads the properties file configuration-test.properties
 * Supports environment variables for sensitive data (e.g., API keys)
 */
public class ConfigurationReader {

    private static Properties properties;

    static {
        try {
            String env = System.getProperty("ENV") != null ? System.getProperty("ENV") : "test";
            String path = switch (env) {
                case "staging" -> "configuration-staging.properties";
                case "dev" -> "configuration-dev.properties";
                default -> "configuration-test.properties";
            };
            FileInputStream input = new FileInputStream(path);
            properties = new Properties();
            properties.load(input);

            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String keyName) {
        // First check if there's an environment variable
        String envValue = System.getenv(keyName);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // Then check system properties (for -D arguments)
        String sysPropValue = System.getProperty(keyName);
        if (sysPropValue != null && !sysPropValue.isEmpty()) {
            return sysPropValue;
        }

        // Finally fall back to properties file
        return properties.getProperty(keyName);
    }

}
