package com.example.steps.security;

import com.example.support.security.JwtUtils;
import com.example.util.ConfigurationReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class JwtSecuritySteps {

    private String baseUrl;
    private String username;
    private String password;
    private String token;
    private Response response;

    @Given("the FakeStore API base is configured")
    public void the_fakestore_api_base_is_configured() {
        baseUrl = ConfigurationReader.get("fakestore_api_base_url");
        assertNotNull("fakestore_api_base_url is not configured", baseUrl);
        username = ConfigurationReader.get("fakestore_username");
        password = ConfigurationReader.get("fakestore_password");
        assertNotNull("fakestore_username must be set in configuration", username);
        assertNotNull("fakestore_password must be set in configuration", password);
    }

    @When("I authenticate to FakeStore with configured credentials")
    public void i_authenticate_to_fakestore_with_configured_credentials() {
        assertNotNull(baseUrl);
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        response = given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/auth/login");
        // FakeStore returns { token: "<jwt>" }
        token = response.jsonPath().getString("token");
    }

    @Then("I receive a JWT bearer token")
    public void i_receive_a_jwt_bearer_token() {
        assertNotNull("No response received", response);
        assertEquals("Expected 200 from /auth/login", 200, response.statusCode());
        assertNotNull("No token field in response", token);
        assertTrue("Token does not look like a JWT", JwtUtils.isLikelyJwt(token));
    }

    @Then("the JWT header includes an algorithm")
    public void the_jwt_header_includes_an_algorithm() {
        assertNotNull("Token not acquired", token);
        var header = JwtUtils.decodeHeader(token);
        assertTrue("Missing JWT header.alg", header.has("alg") && !header.get("alg").getAsString().isBlank());
    }

    @Then("the JWT payload is a valid JSON object")
    public void the_jwt_payload_is_a_valid_json_object() {
        assertNotNull("Token not acquired", token);
        var payload = JwtUtils.decodePayload(token);
        assertNotNull(payload);
        assertTrue(payload.entrySet().size() >= 0);
    }

    @Given("I have a JWT bearer token from FakeStore")
    public void i_have_a_jwt_bearer_token_from_fakestore() {
        if (token == null) {
            i_authenticate_to_fakestore_with_configured_credentials();
            i_receive_a_jwt_bearer_token();
        }
    }

    @When("I call a FakeStore endpoint with the token")
    public void i_call_a_fakestore_endpoint_with_the_token() {
        assertNotNull("Token not acquired", token);
        response = given()
                .baseUri(baseUrl)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products?limit=1");
    }

    @Then("the response status is 200")
    public void the_response_status_is_200() {
        assertNotNull("No response captured", response);
        assertEquals(200, response.statusCode());
    }
}
