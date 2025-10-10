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

public class WeatherApiZipCodePactTest {

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("WeatherAPI", "localhost", 8085, this);

  @Pact(consumer = "QAFrameworkConsumer")
  public RequestResponsePact zipCodeWeatherPact(PactDslWithProvider builder) {
    return builder
        .given("weather data exists for zip code")
        .uponReceiving("a request for current weather by zip code")
        .path("/v1/current.json")
        .method("GET")
        .query("q=90201")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(new PactDslJsonBody()
            .object("location")
              .stringValue("name", "Bell Gardens")
              .stringValue("country", "USA")
              .numberType("lat", 33.9653)
              .numberType("lon", -118.1517)
            .closeObject()
            .object("current")
              .numberType("temp_c", 22.0)
              .integerType("last_updated_epoch", 1609459200)
              .object("condition")
                .stringType("text", "Sunny")
                .stringType("icon", "//cdn.weatherapi.com/weather/64x64/day/113.png")
              .closeObject()
            .closeObject())
        .toPact();
  }

  @Test
  @PactVerification("WeatherAPI")
  public void testCurrentWeatherByZipCode() {
    given()
        .baseUri(mockProvider.getUrl())
    .when()
        .get("/v1/current.json?q=90201")
    .then()
        .statusCode(200)
        .body("location.name", equalTo("Bell Gardens"))
        .body("location.country", equalTo("USA"))
        .body("current.temp_c", notNullValue());
  }
}
