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

public class WeatherApiForecastPactTest {

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("WeatherAPI", "localhost", 8084, this);

  @Pact(consumer = "QAFrameworkConsumer")
  public RequestResponsePact forecastWeatherPact(PactDslWithProvider builder) {
    return builder
        .given("forecast data exists for London")
        .uponReceiving("a request for 3-day weather forecast")
        .path("/v1/forecast.json")
        .method("GET")
        .query("q=London&days=3")
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
            .object("forecast")
              .array("forecastday")
                .object()
                  .stringType("date", "2023-01-01")
                  .object("day")
                    .numberType("maxtemp_c", 12.5)
                    .numberType("mintemp_c", 5.2)
                  .closeObject()
                .closeObject()
                .object()
                  .stringType("date", "2023-01-02")
                  .object("day")
                    .numberType("maxtemp_c", 14.1)
                    .numberType("mintemp_c", 6.8)
                  .closeObject()
                .closeObject()
                .object()
                  .stringType("date", "2023-01-03")
                  .object("day")
                    .numberType("maxtemp_c", 10.3)
                    .numberType("mintemp_c", 3.9)
                  .closeObject()
                .closeObject()
              .closeArray()
            .closeObject())
        .toPact();
  }

  @Test
  @PactVerification("WeatherAPI")
  public void testForecastWeather() {
    given()
        .baseUri(mockProvider.getUrl())
    .when()
        .get("/v1/forecast.json?q=London&days=3")
    .then()
        .statusCode(200)
        .body("location.name", equalTo("London"))
        .body("forecast.forecastday", hasSize(3))
        .body("forecast.forecastday[0].date", notNullValue())
        .body("forecast.forecastday[0].day.maxtemp_c", notNullValue())
        .body("forecast.forecastday[0].day.mintemp_c", notNullValue());
  }
}
