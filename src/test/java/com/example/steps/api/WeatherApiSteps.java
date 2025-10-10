package com.example.steps.api;

import com.example.config.TestConfig;
import com.example.util.CsvDataLoader;
import com.example.util.ScenarioState;
import io.cucumber.java.en.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;


import java.util.List;

import static com.example.support.JsonAsserts.assertCurrentTypes;
import static com.example.support.JsonAsserts.assertForecastDays;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WeatherApiSteps {

    private final ScenarioState state = new ScenarioState();

    // ---------- Common/Given ----------
    @Given("I have a valid WeatherAPI key configured")
    public void i_have_a_valid_key() {
        if (TestConfig.API_KEY == null || TestConfig.API_KEY.isBlank()) {
            throw new IllegalStateException("Set WEATHER_API_KEY as env or -D system property");
        }
    }

    @Given("test cities are loaded from {string}")
    public void load_cities_from_csv(String csv) {
        // Just validate we can read; we’ll iterate in When steps
        List<CsvDataLoader.CityRow> rows = CsvDataLoader.load(csv);
        if (rows.isEmpty()) {
            throw new IllegalStateException("No rows in CSV " + csv);
        }
    }

    // ---------- GET current ----------
    @When("I request current weather for {string}")
    public void request_current(String query) {
        state.setLastQuery(query);
        Response res = given().spec(TestConfig.baseSpec())
                .queryParam("q", query)
                .get("/current.json");
        state.setLastResponse(res);
    }

    @Then("the response status is {int}")
    public void verify_status(int status) {
        state.getLastResponse().then().statusCode(status);
    }

    @Then("the payload has valid current weather types")
    public void payload_has_valid_types() {
        JsonPath jp = state.getLastResponse().then().extract().jsonPath();
//        System.out.println(jp.prettify());
        assertCurrentTypes(jp);
    }

    @Then("the {string} equals {string} if provided")
    public void field_equals_if_provided(String jsonPath, String expected) {
        if (expected == null || expected.isBlank()) return;
        String actual = state.getLastResponse().then().extract().path(jsonPath);
        assertThat(actual, equalTo(expected));
    }

    // ---------- GET forecast ----------
    @When("I request a {int}-day forecast for {string}")
    public void request_forecast(int days, String query) {
        int safeDays = Math.max(1, Math.min(14, days));
        state.setLastQuery(query);
        state.setLastRequestedDays(safeDays);

        Response res = given().spec(TestConfig.baseSpec())
                .queryParam("q", query)
                .queryParam("days", safeDays)
                .get("/forecast.json");
        state.setLastResponse(res);
//        res.body().prettyPrint();
    }

    @Then("the payload contains exactly the requested number of forecast days")
    public void payload_contains_requested_days() {
        JsonPath jp = state.getLastResponse().then().extract().jsonPath();
        assertForecastDays(jp, state.getLastRequestedDays());
    }

    // ---------- POST bulk (negative for free plan) ----------
    @When("I POST a bulk current request")
    public void post_bulk_current() {
        String body = """
          {
            "locations": [
              {"q": "London", "custom_id": "l1"},
              {"q": "90201", "custom_id": "z1"}
            ]
          }
        """;
        Response res = given().spec(TestConfig.baseSpec())
                .contentType(ContentType.JSON)
                .queryParam("q", "bulk")
                .body(body)
                .post("/current.json");
        state.setLastResponse(res);
    }

    @Then("the error code is {int} and message contains {string}")
    public void error_code_and_message(int code, String phrase) {
        state.getLastResponse().then()
                .body("error.code", equalTo(code))
                .body("error.message", containsString(phrase));
    }

    // ---------- Error cases ----------
    @When("I request current weather with no query parameter")
    public void request_current_missing_q() {
        Response res = given().spec(TestConfig.baseSpec())
                .get("/current.json");
        state.setLastResponse(res);
    }

    @When("I request current weather for an unknown location")
    public void request_current_unknown_location() {
        Response res = given().spec(TestConfig.baseSpec())
                .queryParam("q", "this-is-not-a-real-place-xyz")
                .get("/current.json");
//        res.body().prettyPrint();
        state.setLastResponse(res);
    }

    // ---------- CSV parameterisation helper steps ----------
    @When("I request current weather for each city in {string}")
    public void request_current_each_city(String csv) {
        List<CsvDataLoader.CityRow> rows = CsvDataLoader.load(csv);
        for (CsvDataLoader.CityRow row : rows) {
            Response res = given().spec(TestConfig.baseSpec())
                    .queryParam("q", row.query)
                    .get("/current.json");
            res.then().statusCode(200);
            state.setLastResponse(res);
            assertCurrentTypes(res.then().extract().jsonPath());
            if (!row.expectedCountry.isBlank()) {
                String country = res.then().extract().path("location.country");
                if (!row.expectedCountry.equals(country)) {
                    throw new AssertionError("For " + row.query + " expected country '"
                            + row.expectedCountry + "' but got '" + country + "'");
                }
            }
        }
    }

    @When("I request a forecast for each city in {string}")
    public void request_forecast_each_city(String csv) {
        List<CsvDataLoader.CityRow> rows = CsvDataLoader.load(csv);
        for (CsvDataLoader.CityRow row : rows) {
            int safeDays = Math.max(1, Math.min(14, row.forecastDays));
            Response res = given().spec(TestConfig.baseSpec())
                    .queryParam("q", row.query)
                    .queryParam("days", safeDays)
                    .get("/forecast.json");
            res.then().statusCode(200);
            state.setLastResponse(res);
            assertForecastDays(res.then().extract().jsonPath(), safeDays);
        }
    }

    @Then("the response matches schema {string}")
    public void the_response_matches_schema(String schemaPath) {
        state.getLastResponse()
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath(schemaPath));
    }

}
