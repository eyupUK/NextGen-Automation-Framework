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

public class WeatherApiBulkRequestPactTest {

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("WeatherAPI", "localhost", 8080, this);

  @Pact(consumer = "QAFrameworkConsumer")
  public RequestResponsePact bulkRequestPact(PactDslWithProvider builder) {
    return builder
        .given("bulk request on free plan")
        .uponReceiving("a POST request for bulk current weather")
        .path("/v1/current.json")
        .method("POST")
        .headers(Map.of("Content-Type", "application/json"))
        .body(new PactDslJsonBody()
            .array("locations")
              .stringType("London")
              .stringType("Paris")
            .closeArray())
        .willRespondWith()
        .status(400)
        .headers(Map.of("Content-Type", "application/json"))
        .body(new PactDslJsonBody()
            .object("error")
              .integerType("code", 2009)
              .stringType("message", "API key does not have access to the resource. Please check your plan and billing details.")
            .closeObject())
        .toPact();
  }

  @Test
  @PactVerification("WeatherAPI")
  public void testBulkRequestError() {
    given()
        .baseUri(mockProvider.getUrl())
        .contentType("application/json")
        .body("{\"locations\":[\"London\",\"Paris\"]}")
    .when()
        .post("/v1/current.json")
    .then()
        .statusCode(400)
        .body("error.code", equalTo(2009))
        .body("error.message", containsString("does not have access"));
  }
}
