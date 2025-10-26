package com.example.config;


import com.example.util.ConfigurationReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class TestConfig {
    // Prefer system prop or env var override before properties file
    public static final String BASE_URL = System.getProperty("weather_api_base_url",
            System.getenv().getOrDefault("weather_api_base_url",
                    ConfigurationReader.get("weather_api_base_url") != null ? ConfigurationReader.get("weather_api_base_url") : "https://api.weatherapi.com/v1"));

    public static final String API_KEY = System.getProperty("WEATHER_API_KEY",
            System.getenv().getOrDefault("WEATHER_API_KEY", ConfigurationReader.get("WEATHER_API_KEY")));

    public static RequestSpecification baseSpec() {
        RequestSpecBuilder builder = new RequestSpecBuilder().setBaseUri(BASE_URL);
        // Only add key when not using a local/mock base URL
        if (!isLocalMock(BASE_URL) && API_KEY != null && !API_KEY.isBlank()) {
            builder.addQueryParam("key", API_KEY);
        }
        return builder.build();
    }

    private static boolean isLocalMock(String url) {
        if (url == null) return false;
        String u = url.toLowerCase();
        return u.startsWith("http://localhost") || u.startsWith("http://127.0.0.1");
    }
}
