package com.example.steps.security;

import com.example.util.ConfigurationReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import com.example.support.security.OwaspHeaderAsserts;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class SauceDemoSecuritySteps {

    private String baseUrl;
    private Response response;
    private String homepageHtml;

    @Given("I query the SauceDemo homepage")
    public void i_query_the_saucedemo_homepage() {
        baseUrl = ConfigurationReader.get("sauceDemoUrl");
        assertNotNull("sauceDemoUrl is not configured", baseUrl);
        response = given().when().get(baseUrl);
        assertNotNull("No response received from SauceDemo", response);
    }

    @Then("the web response header {string} is present")
    public void the_web_response_header_is_present(String name) {
        assertNotNull(response);
        String value = response.getHeader(name);
        assertNotNull("Expected header to be present: " + name, value);
    }

    @Then("the web response header {string} equals {string}")
    public void the_web_response_header_equals(String name, String expected) {
        assertNotNull(response);
        String value = response.getHeader(name);
        assertNotNull("Missing header: " + name, value);
        assertEquals("Header mismatch for " + name, expected.toLowerCase(), value.toLowerCase());
    }

    @Then("the response has either {string} or a CSP {string} directive")
    public void the_response_has_either_or_a_csp_directive(String headerName, String cspDirective) {
        assertNotNull(response);
        String xfo = response.getHeader(headerName);
        String csp = response.getHeader("Content-Security-Policy");
        boolean hasXfo = xfo != null && !xfo.isBlank();
        boolean hasFrameAncestors = csp != null && csp.toLowerCase().contains(cspDirective.toLowerCase());
        assertTrue("Expected either " + headerName + " or CSP directive '" + cspDirective + "'", hasXfo || hasFrameAncestors);
    }

    @Then("the response meets OWASP baseline for HTTPS endpoints")
    public void the_response_meets_owasp_baseline_for_https_endpoints() {
        assertNotNull("No response captured", response);
        OwaspHeaderAsserts.assertBaseline(response, true);
    }

    @Given("I fetch the SauceDemo homepage HTML")
    public void i_fetch_the_saucedemo_homepage_html() {
        baseUrl = ConfigurationReader.get("sauceDemoUrl");
        assertNotNull("sauceDemoUrl is not configured", baseUrl);
        response = given().when().get(baseUrl);
        assertNotNull(response);
        homepageHtml = response.getBody().asString();
        assertNotNull("Empty homepage HTML", homepageHtml);
    }

    @Then("the page should not reference insecure http resources")
    public void the_page_should_not_reference_insecure_http_resources() {
        assertNotNull(homepageHtml);
        String lower = homepageHtml.toLowerCase();
        // Allow localhost references if any, otherwise assert no http://
        boolean hasInsecure = lower.contains("http://");
        assertFalse("Homepage HTML contains insecure http:// references", hasInsecure);
    }

    @Then("the CSP must not contain {string} or {string}")
    public void the_csp_must_not_contain_or(String first, String second) {
        assertNotNull("No response captured", response);
        String csp = response.getHeader("Content-Security-Policy");
        assertNotNull("Missing Content-Security-Policy header", csp);
        String lc = csp.toLowerCase();
        assertFalse("CSP contains disallowed directive: " + first, lc.contains(first.toLowerCase()));
        assertFalse("CSP contains disallowed directive: " + second, lc.contains(second.toLowerCase()));
    }
}
