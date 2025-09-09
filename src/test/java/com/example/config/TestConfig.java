package com.example.config;


import com.example.util.ConfigurationReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class TestConfig {
    public static final String BASE_URL = "https://api.weatherapi.com/v1";
    public static final String API_KEY = System.getProperty("WEATHERAPI_KEY",
            System.getenv().getOrDefault("WEATHERAPI_KEY", ConfigurationReader.get("WEATHER_API_KEY")));

    public static RequestSpecification baseSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .addQueryParam("key", API_KEY)
                .build();
    }
}

