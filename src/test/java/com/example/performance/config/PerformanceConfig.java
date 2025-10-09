package com.example.performance.config;

import com.example.util.ConfigurationReader;

/**
 * Performance Test Configuration
 * Centralized configuration for performance testing parameters
 */
public class PerformanceConfig {

    // Load from configuration.properties
    public static final String WEATHER_API_BASE_URL = ConfigurationReader.get("weather_api_base_url");
    public static final String FAKESTORE_API_BASE_URL = ConfigurationReader.get("fakestore_api_base_url");

    // Performance Test Parameters (can be overridden via system properties)
    public static final int USERS = Integer.parseInt(System.getProperty("perf.users", "10"));
    public static final int RAMP_UP_TIME = Integer.parseInt(System.getProperty("perf.rampup", "10"));
    public static final int DURATION = Integer.parseInt(System.getProperty("perf.duration", "60"));

    // SLA Thresholds (in milliseconds)
    public static final int RESPONSE_TIME_P95_THRESHOLD = 2000; // 95th percentile
    public static final int RESPONSE_TIME_P99_THRESHOLD = 5000; // 99th percentile
    public static final double SUCCESS_RATE_THRESHOLD = 99.0; // Success rate percentage

    // Load Patterns
    public static final int SPIKE_USERS = 50;
    public static final int STRESS_USERS = 100;

    /**
     * Get Weather API Key - evaluated at runtime to ensure environment variables are loaded
     */
    public static String getWeatherApiKey() {
        String key = ConfigurationReader.get("WEATHER_API_KEY");
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("WEATHER_API_KEY is not set. Please set it as an environment variable or system property.");
        }
        return key;
    }

    // Backward compatibility - deprecated, use getWeatherApiKey() instead
    @Deprecated
    public static final String WEATHER_API_KEY = System.getenv("WEATHER_API_KEY") != null ?
        System.getenv("WEATHER_API_KEY") :
        (System.getProperty("WEATHER_API_KEY") != null ?
            System.getProperty("WEATHER_API_KEY") :
            "");

    private PerformanceConfig() {
        // Utility class
    }
}
