package com.example.contract;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class WeatherApiCoordinatesPactTest {

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("WeatherAPI", "localhost", 8082, this);

  @Pact(consumer = "QAFrameworkConsumer")
  public RequestResponsePact coordinatesWeatherPact(PactDslWithProvider builder) {
    return builder
        .given("weather data exists for coordinates")
        .uponReceiving("a request for current weather by coordinates")
        .path("/v1/current.json")
        .method("GET")
        .query("q=48.8567,2.3508")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(new PactDslJsonBody()
            .object("location")
              .stringValue("name", "Paris")
              .stringValue("country", "France")
              .numberType("lat", 48.8567)
              .numberType("lon", 2.3508)
            .closeObject()
            .object("current")
              .numberType("temp_c", 18.2)
              .integerType("last_updated_epoch", 1609459200)
              .object("condition")
                .stringType("text", "Clear")
                .stringType("icon", "//cdn.weatherapi.com/weather/64x64/day/113.png")
              .closeObject()
            .closeObject())
        .toPact();
  }

  @Test
  @PactVerification("WeatherAPI")
  public void testCurrentWeatherByCoordinates() {
    given()
        .baseUri(mockProvider.getUrl())
    .when()
        .get("/v1/current.json?q=48.8567,2.3508")
    .then()
        .statusCode(200)
        .body("location.name", equalTo("Paris"))
        .body("location.country", equalTo("France"))
        .body("current.temp_c", notNullValue());
  }
}
