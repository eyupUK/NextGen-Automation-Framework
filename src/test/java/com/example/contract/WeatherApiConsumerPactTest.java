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

public class WeatherApiConsumerPactTest {

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("WeatherAPI", "localhost", 8081, this);

  // Contract: London current weather only
  @Pact(consumer = "QAFrameworkConsumer")
  public RequestResponsePact londonWeatherPact(PactDslWithProvider builder) {
    return builder
        .given("weather data exists for London")
        .uponReceiving("a request for current weather in London")
        .path("/v1/current.json")
        .method("GET")
        .query("q=London")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(new PactDslJsonBody()
            .object("location")
              .stringValue("name", "London")
              .stringValue("country", "United Kingdom")
              .numberType("lat", 51.5074)
              .numberType("lon", -0.1278)
            .closeObject()
            .object("current")
              .numberType("temp_c", 15.5)
              .integerType("last_updated_epoch", 1609459200)
              .object("condition")
                .stringType("text", "Partly cloudy")
                .stringType("icon", "//cdn.weatherapi.com/weather/64x64/day/116.png")
              .closeObject()
            .closeObject())
        .toPact();
  }

  @Test
  @PactVerification("WeatherAPI")
  public void testCurrentWeatherForLondon() {
      String baseUrl = mockProvider.getUrl();
      given()
              .baseUri(baseUrl)
              .when()
              .get("/v1/current.json?q=London")
              .then()
              .statusCode(200)
              .body("location.name", equalTo("London"))
              .body("location.country", equalTo("United Kingdom"))
              .body("location.lat", is(51.5074f))
              .body("location.lon", is(-0.1278f))
              .body("current.temp_c", notNullValue())
              .body("current.last_updated_epoch", notNullValue())
              .body("current.condition.text", notNullValue())
              .body("current.condition.icon", notNullValue());
  }
}
