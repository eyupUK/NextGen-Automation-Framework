package com.example.steps.security;

import com.example.config.TestConfig;
import com.example.util.ConfigurationReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import com.example.support.security.OwaspHeaderAsserts;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class WeatherApiSecuritySteps {

    private String baseUrl;
    private String apiKey;
    private Response response;
    private Response rateLimitResponse;
    private Response nextResponse;
    private boolean saw429;

    @Given("the Weather API base is configured")
    public void the_weather_api_base_is_configured() {
        baseUrl = ConfigurationReader.get("weather_api_base_url");
        assertNotNull("weather_api_base_url is not configured", baseUrl);
        // API key may be null; steps that require a valid key assert presence separately
        apiKey = TestConfig.API_KEY;
    }

    @When("I call current weather with an invalid API key")
    public void i_call_current_weather_with_an_invalid_api_key() {
        assertNotNull("Weather API base URL must be set", baseUrl);
        response = given()
                .baseUri(baseUrl)
                .accept(ContentType.JSON)
                .queryParam("key", "invalid")
                .queryParam("q", "London")
                .when()
                .get("/current.json");
    }

    @Then("the response status is not 200")
    public void the_response_status_is_not_200() {
        assertNotNull("No response captured", response);
        assertNotEquals("Expected non-200 when using invalid API key", 200, response.statusCode());
    }

    @Then("the error payload contains a code and message")
    public void the_error_payload_contains_a_code_and_message() {
        assertNotNull(response);
        Integer code = response.jsonPath().get("error.code");
        String message = response.jsonPath().get("error.message");
        assertNotNull("Expected error.code in payload", code);
        assertNotNull("Expected error.message in payload", message);
        assertFalse("Empty error message", message.isBlank());
    }

    @When("I request current weather for {string} with a valid API key")
    public void i_request_current_weather_for_with_a_valid_API_key(String payload) {
        assertNotNull("Weather API base URL must be set", baseUrl);
        assertNotNull("WEATHER_API_KEY must be set for this step", apiKey);
        response = given()
                .baseUri(baseUrl)
                .accept(ContentType.JSON)
                .queryParam("key", apiKey)
                .queryParam("q", payload)
                .when()
                .get("/current.json");
    }

    @Then("the response status is not a server error")
    public void the_response_status_is_not_a_server_error() {
        assertNotNull(response);
        int status = response.statusCode();
        assertTrue("Unexpected server error status: " + status, status < 500);
    }

    @Then("the response content type is JSON")
    public void the_response_content_type_is_json() {
        assertNotNull(response);
        String contentType = response.getContentType();
        assertNotNull(contentType);
        assertTrue("Content-Type is not JSON: " + contentType,
                contentType.toLowerCase().contains("application/json"));
    }

    @Then("the response header {string} equals {string}")
    public void the_response_header_equals(String name, String expected) {
        assertNotNull(response);
        String value = response.getHeader(name);
        assertNotNull("Missing header: " + name, value);
        assertEquals("Header mismatch for " + name, expected.toLowerCase(), value.toLowerCase());
    }

    @Then("the response header {string} is present")
    public void the_response_header_is_present(String name) {
        assertNotNull(response);
        assertNotNull("Missing header: " + name, response.getHeader(name));
    }

    @Then("the response meets OWASP API baseline for HTTPS endpoints")
    public void the_response_meets_owasp_api_baseline_for_https_endpoints() {
        assertNotNull(response);
        OwaspHeaderAsserts.assertApiBaseline(response, true);
    }

    @When("I rapidly call current weather {int} times with a valid API key")
    public void i_rapidly_call_current_weather_times_with_a_valid_api_key(int times) {
        // Allow override via system property: -DrateLimit.calls=NN
        try {
            String override = System.getProperty("rateLimit.calls");
            if (override != null && override.matches("\\d+")) {
                times = Integer.parseInt(override);
            }
        } catch (Exception ignored) {}
        assertNotNull("Weather API base URL must be set", baseUrl);
        assertNotNull("WEATHER_API_KEY must be set for this step", apiKey);
        saw429 = false;
        rateLimitResponse = null;
        Response last = null;
        for (int i = 0; i < times; i++) {
            last = given()
                    .baseUri(baseUrl)
                    .accept(ContentType.JSON)
                    .queryParam("key", apiKey)
                    .queryParam("q", "London")
                    .when()
                    .get("/current.json");
            if (last.statusCode() == 429 && !saw429) {
                saw429 = true;
                rateLimitResponse = last;
            }
        }
        // Keep the last response for general assertions; if we saw a 429, switch response to it so header checks apply
        if (saw429 && rateLimitResponse != null) {
            response = rateLimitResponse;
        } else {
            response = last;
        }
    }

    @Then("I eventually receive a 429 status")
    public void i_eventually_receive_a_429_status() {
        assertTrue("Did not observe a 429 Too Many Requests during rapid calls", saw429);
        assertNotNull("Expected to capture a 429 response", rateLimitResponse);
    }

    @When("I wait the Retry-After duration then retry the request")
    public void i_wait_the_retry_after_duration_then_retry_the_request() {
        assertNotNull("No 429 response captured for Retry-After", response);
        String ra = response.getHeader("Retry-After");
        // Default to a small wait if header missing or unparsable
        long waitMillis = 2000L;
        try {
            if (ra != null) {
                ra = ra.trim();
                if (ra.matches("\\d+")) {
                    waitMillis = Math.min(10000L, Long.parseLong(ra) * 1000L);
                } else {
                    ZonedDateTime dt = ZonedDateTime.parse(ra, DateTimeFormatter.RFC_1123_DATE_TIME);
                    long delta = Duration.between(ZonedDateTime.now(dt.getZone()), dt).toMillis();
                    waitMillis = Math.min(10000L, Math.max(0L, delta));
                }
            }
        } catch (Exception ignored) {}
        try { Thread.sleep(waitMillis); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        assertNotNull("WEATHER_API_KEY must be set for retry", apiKey);
        nextResponse = given()
                .baseUri(baseUrl)
                .accept(ContentType.JSON)
                .queryParam("key", apiKey)
                .queryParam("q", "London")
                .when()
                .get("/current.json");
    }

    @Then("the next response status is not 429")
    public void the_next_response_status_is_not_429() {
        assertNotNull("No next response captured", nextResponse);
        assertNotEquals(429, nextResponse.statusCode());
    }


    @When("I preflight current weather CORS for origin {string} method {string} and request headers {string}")
    public void i_preflight_current_weather_cors_for_origin_method_and_request_headers(String origin, String method, String headers) {
        assertNotNull("Weather API base URL must be set", baseUrl);
        response = given()
                .baseUri(baseUrl)
                .header("Origin", origin)
                .header("Access-Control-Request-Method", method)
                .header("Access-Control-Request-Headers", headers)
                .when()
                .options("/current.json");
    }

    @Then("the response header {string} contains {string}")
    public void the_response_header_contains(String name, String expected) {
        assertNotNull(response);
        String value = response.getHeader(name);
        assertNotNull("Missing header: " + name, value);
        assertTrue("Header '" + name + "' does not contain '" + expected + "' (value: " + value + ")",
                value.toLowerCase().contains(expected.toLowerCase()));
    }

    @Then("the response header {string} contains all of {string}")
    public void the_response_header_contains_all_of(String name, String csvList) {
        assertNotNull(response);
        String value = response.getHeader(name);
        assertNotNull("Missing header: " + name, value);
        String lc = value.toLowerCase();
        for (String token : csvList.split(",")) {
            String t = token.trim().toLowerCase();
            if (!t.isEmpty()) {
                assertTrue("Header '" + name + "' missing token '" + t + "' in value: " + value, lc.contains(t));
            }
        }
    }

    @Then("the response should not allow origin {string}")
    public void the_response_should_not_allow_origin(String origin) {
        assertNotNull(response);
        String allowOrigin = response.getHeader("Access-Control-Allow-Origin");
        assertTrue(
                "Expected disallowed origin not to be echoed; got: " + allowOrigin,
                allowOrigin == null || (!allowOrigin.equalsIgnoreCase(origin) && !"*".equals(allowOrigin))
        );
    }

    @Then("the response meets OWASP API baseline for HTTPS endpoints without HSTS")
    public void the_response_meets_owasp_api_baseline_for_https_endpoints_without_hsts() {
        assertNotNull(response);
        OwaspHeaderAsserts.assertApiBaseline(response, true, false);
    }
}
