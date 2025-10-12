package com.example.steps.performance;

import com.example.config.TestConfig;
import com.example.performance.junit.tests.WeatherApiPerformanceTest;
import com.example.util.ConfigurationReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNull;

public class WeatherApiPerfSteps {

    private Throwable lastError;
    private WeatherApiPerformanceTest perf;


    @Given("weather api is ready for performance tests")
    public void weatherApiIsReadyForPerformanceTests() {
        String baseUrl = ConfigurationReader.get("weather_api_base_url");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://api.weatherapi.com/v1";
        }
        String apiKey = TestConfig.API_KEY;
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set WEATHER_API_KEY as env or -D system property");
        }
        given().baseUri(baseUrl)
                .queryParam("key", apiKey)
                .queryParam("q", "London")
                .when().get("/current.json")
                .then().statusCode(200);
        System.out.println("====>  Weather API is ready for performance tests");
    }


    @When("I run the load performance for Weather API")
    public void iRunTheLoadPerformanceForWeatherAPI() {
        try {
            lastError = null;
            perf = new WeatherApiPerformanceTest();
            perf.setUp();
            perf.testCurrentWeatherEndpointUnderLoad();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @When("I run the stress performance for Weather API")
    public void iRunTheStressPerformanceForWeatherAPI() {
        try {
            lastError = null;
            perf = new WeatherApiPerformanceTest();
            perf.setUp();
            perf.testForecastEndpointStress();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @When("I run the spike performance for Weather API")
    public void iRunTheSpikePerformanceForWeatherAPI() {
        try {
            lastError = null;
            perf = new WeatherApiPerformanceTest();
            perf.setUp();
            perf.testSpikeLoad();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @When("I run the endurance performance for Weather API")
    public void iRunTheEndurancePerformanceForWeatherAPI() {
        try {
            lastError = null;
            perf = new WeatherApiPerformanceTest();
            perf.setUp();
            perf.testEndurance();
        } catch (Throwable t) {
            lastError = t;
        }
    }


    @Then("it completes successfully loading weather data")
    public void itCompletesSuccessfullyLoadingWeatherData() throws Exception {
        if (lastError != null) {
            lastError.printStackTrace();
        }
        assertNull("Performance example failed: " + (lastError == null ? "" : lastError.getMessage()), lastError);
        new WeatherApiPerformanceTest().tearDown();
    }


    @Then("it completes successfully performance testing of weather api")
    public void itCompletesSuccessfullyPerformanceTestingOfWeatherApi() throws Exception {
        if (lastError != null) {
            lastError.printStackTrace();
        }
        assertNull("Performance example failed: " + (lastError == null ? "" : lastError.getMessage()), lastError);
        perf.tearDown();
    }
}
