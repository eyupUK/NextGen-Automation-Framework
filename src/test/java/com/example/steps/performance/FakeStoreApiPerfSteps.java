package com.example.steps.performance;

import com.example.performance.tests.FakeStorePerformanceTest;
import com.example.performance.tests.WeatherApiPerformanceTest;
import com.example.util.ConfigurationReader;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNull;

public class FakeStoreApiPerfSteps {

    private Throwable lastError;
    private FakeStorePerformanceTest perf;


    @Given("fake store api is ready for performance tests")
    public void fakeStoreApiIsReadyForPerformanceTests() {
        String baseUrl = ConfigurationReader.get("fakestore_api_base_url");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://fakestoreapi.com";
        }
        given().baseUri(baseUrl).when().get("/products").then().statusCode(200);
        System.out.println("====>  Fake Store API is ready for performance tests");
    }

    @When("I run the load performance for Products endpoint")
    public void iRunTheLoadPerformanceForProductsEndpoint() {
        try {
            lastError = null;
            perf = new FakeStorePerformanceTest();
            perf.setUp();
            perf.testProductsEndpointUnderLoad();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @When("I run the load performance for Product Detail endpoint")
    public void iRunTheLoadPerformanceForProductDetailEndpoint() {
        try {
            lastError = null;
            perf = new FakeStorePerformanceTest();
            perf.setUp();
            perf.testProductDetailUnderLoad();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @Then("it completes successfully performance testing of fake store api")
    public void itCompletesSuccessfullyPerformanceTestingOfFakeStoreApi() {
        if (lastError != null) {
            lastError.printStackTrace();
        }
        assertNull("Performance example failed: " + (lastError == null ? "" : lastError.getMessage()), lastError);
    }
}
