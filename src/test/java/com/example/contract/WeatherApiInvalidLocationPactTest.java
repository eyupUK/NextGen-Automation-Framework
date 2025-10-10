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

public class WeatherApiInvalidLocationPactTest {

    // Let Pact pick a free port; it will start/stop the mock server for you
    @Rule
    public PactProviderRule mockProvider =
            new PactProviderRule("WeatherAPI", "localhost", 8084, this);

    @Pact(consumer = "QAFrameworkConsumer")
    public RequestResponsePact invalidLocationPact(PactDslWithProvider builder) {
        return builder
                .given("invalid location provided")
                .uponReceiving("a request for unknown location")
                .path("/v1/current.json")
                .method("GET")
                .query("q=UnknownCity12345")
                .willRespondWith()
                .status(400)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .object("error")
                        .integerType("code", 1006)
                        .stringType("message", "No location found matching parameter 'q'.")
                        .closeObject())
                .toPact();
    }

    @Test
    @PactVerification("WeatherAPI") // verify against this provider’s pact
    public void testInvalidLocationError() {
        String baseUrl = mockProvider.getUrl();

        given()
                .baseUri(baseUrl)
                .when()
                .get("/v1/current.json?q=UnknownCity12345")
                .then()
                .statusCode(400)
                .body("error.code", equalTo(1006))
                .body("error.message", containsString("No location found matching parameter 'q'"));
    }
}
