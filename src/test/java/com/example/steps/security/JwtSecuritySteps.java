package com.example.steps.security;

import com.example.util.ConfigurationReader;
import com.example.support.security.JwtUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class JwtSecuritySteps {

    private String baseUrl;
    private String email;
    private final String password = "pass123";
    private String token;
    private Response response;

    @Given("the Demo JWT API base is configured")
    public void the_demo_jwt_api_base_is_configured() {
        // Allow override via configuration, otherwise default to EscuelaJS public API
        String cfgBase = ConfigurationReader.get("demo_jwt_api_base_url");
        baseUrl = (cfgBase == null || cfgBase.isBlank()) ? "https://api.escuelajs.co/api/v1" : cfgBase;
        assertNotNull("Demo JWT base URL could not be determined", baseUrl);
    }

    private void registerUserIfNeeded() {
        if (email == null) {
            email = "john." + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "@example.com";
        }
        Map<String, Object> user = new HashMap<>();
        user.put("name", "John Doe");
        user.put("email", email);
        user.put("password", password);
        user.put("avatar", "https://i.pravatar.cc/150?img=3");

        int attempts = 0;
        long backoffMs = 500; // initial backoff
        while (true) {
            attempts++;
            try {
                Response createResp = given()
                        .baseUri(baseUrl)
                        .contentType(ContentType.JSON)
                        .body(user)
                        .when()
                        .post("/users");
                int status = createResp.statusCode();
                if (status >= 200 && status < 300) {
                    return;
                }
                // transient server or rate limiting -> retry
                if ((status == 429 || status >= 500) && attempts < 4) {
                    try { Thread.sleep(backoffMs); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                    backoffMs = Math.min(4000, backoffMs * 2);
                    continue;
                }
                fail("User creation failed with status: " + status);
            } catch (Exception e) {
                if (attempts < 4) {
                    try { Thread.sleep(backoffMs); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                    backoffMs = Math.min(4000, backoffMs * 2);
                    continue;
                }
                fail("User creation failed after retries: " + e.getMessage());
            }
        }
    }

    @When("I register a demo user and authenticate")
    public void i_register_a_demo_user_and_authenticate() {
        assertNotNull(baseUrl);
        // Create a unique email to avoid duplicate user issues on public API
        email = "john." + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "@example.com";
        registerUserIfNeeded();

        // Authenticate: POST /auth/login { email, password } -> { access_token }
        Map<String, Object> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        response = given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post("/auth/login");

        token = response.jsonPath().getString("access_token");
    }

    @Then("I receive a JWT bearer token")
    public void i_receive_a_jwt_bearer_token() {
        assertNotNull("No response received", response);
        assertTrue("Expected 2xx from /auth/login, got: " + response.statusCode(), response.statusCode() >= 200 && response.statusCode() < 300);
        assertNotNull("No access_token field in response", token);
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
        // Ensure payload contains at least one claim
        assertFalse("Payload should not be empty", payload.entrySet().isEmpty());
        // Optional standard claims sanity
        if (payload.has("sub")) {
            assertFalse("sub claim is blank", payload.get("sub").getAsString().isBlank());
        }
        if (payload.has("iss")) {
            assertFalse("iss claim is blank", payload.get("iss").getAsString().isBlank());
        }
        if (payload.has("aud")) {
            assertFalse("aud claim is blank", payload.get("aud").getAsString().isBlank());
        }
        if (payload.has("exp")) {
            assertTrue("exp claim must be a number", payload.get("exp").isJsonPrimitive() && payload.get("exp").getAsJsonPrimitive().isNumber());
            // Not expired with small clock skew
            assertFalse("Token is expired per exp claim", JwtUtils.isExpired(token, 60));
        }
        if (payload.has("iat")) {
            assertTrue("iat claim must be a number", payload.get("iat").isJsonPrimitive() && payload.get("iat").getAsJsonPrimitive().isNumber());
        }
        // Optional exact match assertions if configured
        String expectedIss = ConfigurationReader.get("jwt.expected_iss");
        if (expectedIss != null && !expectedIss.isBlank() && payload.has("iss")) {
            assertEquals("Unexpected issuer (iss)", expectedIss, payload.get("iss").getAsString());
        }
        String expectedAud = ConfigurationReader.get("jwt.expected_aud");
        if (expectedAud != null && !expectedAud.isBlank() && payload.has("aud")) {
            assertEquals("Unexpected audience (aud)", expectedAud, payload.get("aud").getAsString());
        }
    }

    @Given("I have a JWT bearer token")
    public void i_have_a_jwt_bearer_token() {
        if (token == null) {
            i_register_a_demo_user_and_authenticate();
            i_receive_a_jwt_bearer_token();
        }
    }

    @When("I call a protected demo endpoint with the token")
    public void i_call_a_protected_demo_endpoint_with_the_token() {
        assertNotNull("Token not acquired", token);
        response = given()
                .baseUri(baseUrl)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/auth/profile");
    }

    @Then("the response status is 200")
    public void the_response_status_is_200() {
        assertNotNull("No response captured", response);
        assertEquals(200, response.statusCode());
    }

    @Then("the response status code is {int}")
    public void the_response_status_code_is(int expected) {
        assertNotNull("No response captured", response);
        assertEquals(expected, response.statusCode());
    }

    // Negative cases

    @When("I ensure a demo user is registered")
    public void i_ensure_a_demo_user_is_registered() {
        assertNotNull(baseUrl);
        if (email == null) {
            email = "john." + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "@example.com";
        }
        registerUserIfNeeded();
    }

    @When("I authenticate with an incorrect password")
    public void i_authenticate_with_an_incorrect_password() {
        assertNotNull(baseUrl);
        assertNotNull("Email must be set by registration step", email);
        Map<String, Object> login = new HashMap<>();
        login.put("email", email);
        login.put("password", "wrong-" + password);
        response = given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(login)
                .when()
                .post("/auth/login");
    }

    @Then("the token endpoint returns an error status")
    public void the_token_endpoint_returns_an_error_status() {
        assertNotNull("No response captured", response);
        int sc = response.statusCode();
        assertTrue("Expected 4xx or 5xx status, got: " + sc, sc >= 400);
        String at = null;
        try { at = response.jsonPath().getString("access_token"); } catch (Exception ignored) {}
        assertTrue("Should not receive access_token on error", at == null || at.isBlank());
    }

    @When("I call a protected demo endpoint with a tampered token")
    public void i_call_a_protected_demo_endpoint_with_a_tampered_token() {
        assertNotNull("Token not acquired", token);
        String tampered = token;
        if (tampered.length() > 10) {
            tampered = tampered.substring(0, tampered.length() - 1) + (tampered.endsWith("a") ? "b" : "a");
        } else {
            tampered = token + "x";
        }
        response = given()
                .baseUri(baseUrl)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + tampered)
                .when()
                .get("/auth/profile");
    }

    @Then("the response status is one of 401 or 403")
    public void the_response_status_is_one_of_401_or_403() {
        assertNotNull("No response captured", response);
        int sc = response.statusCode();
        assertTrue("Expected 401 or 403, got: " + sc, sc == 401 || sc == 403);
    }
}
