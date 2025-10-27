package com.example.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * Reads configuration from environment-specific properties with sensible fallbacks.
 * Order of precedence for values returned by get(key):
 *  1) Environment variables
 *  2) System properties (-Dkey=value)
 *  3) Loaded properties file (environment-specific if present, otherwise default)
 */
public class ConfigurationReader {

    private static Properties properties;

    static {
        properties = new Properties();
        String env = resolveEnv();
        String fileName = switch (env) {
            case "staging" -> "configuration-staging.properties";
            case "dev" -> "configuration-dev.properties";
            default -> "configuration-test.properties";
        };

        // Prefer env-specific file if it exists; otherwise fall back to configuration.properties
        Path envPath = Path.of(fileName);
        Path fallbackPath = Path.of("configuration.properties");

        String chosen;
        if (Files.exists(envPath)) {
            chosen = envPath.toString();
        } else if (Files.exists(fallbackPath)) {
            chosen = fallbackPath.toString();
        } else {
            chosen = null;
        }

        System.out.println("[Configuration] ENV=" + env + (chosen != null ? ", file=" + chosen : ", file=<none>"));

        if (chosen != null) {
            try (InputStream in = new FileInputStream(chosen)) {
                properties.load(in);
            } catch (Exception e) {
                System.out.println("[Configuration] Failed to load properties from " + chosen + ": " + e.getMessage());
            }
        } else {
            System.out.println("[Configuration] No properties file found; relying on env vars and -D system properties.");
        }
    }

    private static String resolveEnv() {
        // System properties take precedence for selecting the file
        String env = System.getProperty("ENV");
        if (env == null || env.isBlank()) env = System.getProperty("env");
        // Then environment variables (support both ENV and TEST_ENV to avoid shell conflicts)
        if (env == null || env.isBlank()) env = System.getenv("ENV");
        if (env == null || env.isBlank()) env = System.getenv("TEST_ENV");
        if (env == null || env.isBlank()) env = "test";
        return env.toLowerCase(Locale.ROOT).trim();
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
