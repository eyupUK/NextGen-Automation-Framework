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


public class WeatherApiErrorsPactTest {

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("WeatherAPI", "localhost", 8083, this);

  @Pact(consumer = "QAFrameworkConsumer")
  public RequestResponsePact missingQueryErrorPact(PactDslWithProvider builder) {
    return builder
        .given("no query parameter provided")
        .uponReceiving("a request with missing q parameter")
        .path("/v1/current.json")
        .method("GET")
        .willRespondWith()
        .status(400)
        .headers(Map.of("Content-Type", "application/json"))
        .body(new PactDslJsonBody()
            .object("error")
              .integerType("code", 1003)
              .stringType("message", "Parameter q is missing.")
            .closeObject())
        .toPact();
  }

  @Test
  @PactVerification("WeatherAPI")
  public void testMissingQueryParameterError() {
    given()
        .baseUri(mockProvider.getUrl())
    .when()
        .get("/v1/current.json")
    .then()
        .statusCode(400)
        .body("error.code", equalTo(1003))
        .body("error.message", containsString("Parameter q is missing"));
  }
}
