package com.example.steps.security;

import com.example.support.security.JwtUtils;
import com.example.util.ConfigurationReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class OAuthSecuritySteps {

    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String scope;
    private String probeUrl;

    private String accessToken;
    private String tokenType;
    private Response response;

    @Given("OAuth 2.0 client credentials are configured")
    public void oauth_client_credentials_are_configured() {
        tokenUrl = ConfigurationReader.get("oauth.token_url");
        clientId = ConfigurationReader.get("oauth.client_id");
        clientSecret = ConfigurationReader.get("oauth.client_secret");
        scope = ConfigurationReader.get("oauth.scope");
        probeUrl = ConfigurationReader.get("oauth.probe_url");
        assertNotNull("oauth.token_url must be configured for @oauth tests", tokenUrl);
        assertNotNull("oauth.client_id must be configured for @oauth tests", clientId);
        assertNotNull("oauth.client_secret must be configured for @oauth tests", clientSecret);
    }

    @When("I request an OAuth access token")
    public void i_request_an_oauth_access_token() {
        var req = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", clientId)
                .formParam("client_secret", clientSecret);
        if (scope != null && !scope.isBlank()) {
            req = req.formParam("scope", scope);
        }
        response = req.when().post(tokenUrl);
        accessToken = response.jsonPath().getString("access_token");
        tokenType = response.jsonPath().getString("token_type");
    }

    @Then("I receive an access token of type Bearer")
    public void i_receive_an_access_token_of_type_bearer() {
        assertNotNull("No response from token endpoint", response);
        assertTrue("Unexpected HTTP status from token endpoint: " + response.statusCode(), response.statusCode() < 400);
        assertNotNull("Missing access_token in response", accessToken);
        assertNotNull("Missing token_type in response", tokenType);
        assertTrue("token_type is not Bearer (case-insensitive): " + tokenType, tokenType.equalsIgnoreCase("Bearer"));
    }

    @Then("if the access token is a JWT it has a header algorithm and a JSON payload")
    public void if_token_is_jwt_validate_header_and_payload() {
        if (JwtUtils.isLikelyJwt(accessToken)) {
            var header = JwtUtils.decodeHeader(accessToken);
            assertTrue("Missing JWT header.alg", header.has("alg") && !header.get("alg").getAsString().isBlank());
            var payload = JwtUtils.decodePayload(accessToken);
            assertNotNull(payload);
        }
    }

    @Given("I have an OAuth access token")
    public void i_have_an_oauth_access_token() {
        if (accessToken == null) {
            i_request_an_oauth_access_token();
            i_receive_an_access_token_of_type_bearer();
        }
    }

    @When("I call the OAuth probe endpoint with the token")
    public void i_call_the_oauth_probe_endpoint_with_the_token() {
        assertNotNull("oauth.probe_url must be configured to call a protected resource", probeUrl);
        response = given()
                .header("Authorization", "Bearer " + accessToken)
                .accept(ContentType.JSON)
                .when()
                .get(probeUrl);
    }

    @Then("the response status is a successful 2xx")
    public void the_response_status_is_a_successful_2xx() {
        assertNotNull("No response captured", response);
        int status = response.statusCode();
        assertTrue("Expected 2xx status, got " + status, status >= 200 && status < 300);
    }
}
