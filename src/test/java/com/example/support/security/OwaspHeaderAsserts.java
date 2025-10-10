package com.example.support.security;

import io.restassured.response.Response;

import static org.junit.Assert.*;

public final class OwaspHeaderAsserts {
    private OwaspHeaderAsserts() {}

    /**
     * Backward compatibility: treat generic baseline as HTML page baseline.
     */
    public static void assertBaseline(Response response, boolean httpsExpected) {
        assertHtmlBaseline(response, httpsExpected);
    }

    /**
     * Minimal baseline for JSON/API endpoints.
     * - X-Content-Type-Options: nosniff
     * - HSTS required for HTTPS endpoints
     */
    public static void assertApiBaseline(Response response, boolean httpsExpected) {
        assertApiBaseline(response, httpsExpected, true);
    }

    /**
     * Minimal baseline for JSON/API endpoints, with configurable HSTS requirement.
     * - X-Content-Type-Options: nosniff
     * - HSTS required for HTTPS endpoints if requireHsts is true
     */
    public static void assertApiBaseline(Response response, boolean httpsExpected, boolean requireHsts) {
        assertNotNull("Response is null", response);
        String xcto = response.getHeader("X-Content-Type-Options");
        assertNotNull("Missing X-Content-Type-Options", xcto);
        assertEquals("nosniff", xcto.toLowerCase());
        if (httpsExpected && requireHsts) {
            String hsts = response.getHeader("Strict-Transport-Security");
            assertNotNull("Missing Strict-Transport-Security for HTTPS", hsts);
        }
    }

    /**
     * Strong baseline for HTML pages.
     * - X-Content-Type-Options: nosniff
     * - X-Frame-Options present OR CSP frame-ancestors present
     * - Referrer-Policy present
     * - Permissions-Policy present
     * - HSTS required for HTTPS endpoints
     */
    public static void assertHtmlBaseline(Response response, boolean httpsExpected) {
        assertNotNull("Response is null", response);

        // X-Content-Type-Options
        String xcto = response.getHeader("X-Content-Type-Options");
        assertNotNull("Missing X-Content-Type-Options", xcto);
        assertEquals("nosniff", xcto.toLowerCase());

        // X-Frame-Options OR CSP frame-ancestors
        String xfo = response.getHeader("X-Frame-Options");
        String csp = response.getHeader("Content-Security-Policy");
        boolean hasXfo = xfo != null && !xfo.isBlank();
        boolean hasFrameAncestors = csp != null && csp.toLowerCase().contains("frame-ancestors");
        assertTrue("Expected X-Frame-Options or CSP frame-ancestors", hasXfo || hasFrameAncestors);

        // Referrer-Policy present
        String refPol = response.getHeader("Referrer-Policy");
        assertNotNull("Missing Referrer-Policy", refPol);

        // Permissions-Policy present (even if empty)
        String permPol = response.getHeader("Permissions-Policy");
        assertNotNull("Missing Permissions-Policy", permPol);

        // HSTS for HTTPS endpoints
        if (httpsExpected) {
            String hsts = response.getHeader("Strict-Transport-Security");
            assertNotNull("Missing Strict-Transport-Security for HTTPS", hsts);
        }
    }
}
