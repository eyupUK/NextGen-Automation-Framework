package com.example.support.security;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Lightweight JWT helper for tests. It does not validate signatures; it only
 * inspects structure and decodes header/payload as JSON.
 */
public final class JwtUtils {

    private JwtUtils() { }

    /**
     * Quick heuristic to determine whether a string appears to be a JWT.
     * @param token candidate token
     * @return true if it looks like three base64url parts separated by dots
     */
    public static boolean isLikelyJwt(String token) {
        System.out.println(token);
        if (token == null) return false;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;
        // Basic base64url check: only URL-safe characters
        for (String p : parts) {
            if (p.isEmpty()) return false;
            System.out.println(p);
            if (!p.matches("[A-Za-z0-9_-]+")) return false;
        }
        return true;
    }

    /**
     * Decode the JWT header to a JsonObject. Returns an empty object on error.
     */
    public static JsonObject decodeHeader(String jwt) {
        String json = decodePartToString(jwt, 0);
        System.out.printf("\npart 0: %s", json);
        return safeParse(json);
    }

    /**
     * Decode the JWT payload (claims) to a JsonObject. Returns an empty object on error.
     */
    public static JsonObject decodePayload(String jwt) {
        String json = decodePartToString(jwt, 1);
        System.out.printf("\npart 1: %s", json);
        return safeParse(json);
    }

    /**
     * Convenience: get a claim as String if present, otherwise null.
     */
    public static String getClaimAsString(String jwt, String claim) {
        JsonObject payload = decodePayload(jwt);
        if (payload.has(claim)) {
            JsonElement e = payload.get(claim);
            if (e != null && !e.isJsonNull()) return e.getAsString();
        }
        return null;
    }

    /**
     * Convenience: get a claim as Long if present and numeric, otherwise null.
     */
    public static Long getClaimAsLong(String jwt, String claim) {
        JsonObject payload = decodePayload(jwt);
        if (payload.has(claim)) {
            JsonElement e = payload.get(claim);
            if (e != null && e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
                try { return e.getAsLong(); } catch (Exception ignored) { }
            }
        }
        return null;
    }

    /**
     * Get the exp (seconds since epoch) if present.
     */
    public static Long getExpiration(String jwt) {
        return getClaimAsLong(jwt, "exp");
    }

    /**
     * True if the token has an exp claim and it's in the past allowing some clock skew.
     */
    public static boolean isExpired(String jwt, long clockSkewSeconds) {
        Long exp = getExpiration(jwt);
        if (exp == null) return false; // no exp -> cannot determine
        long now = Instant.now().getEpochSecond();
        return (exp + clockSkewSeconds) < now;
    }

    private static String decodePartToString(String jwt, int index) {
        if (jwt == null) return null;
        String[] parts = jwt.split("\\.");
        if (parts.length <= index) return null;
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(padBase64(parts[index]));
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String padBase64(String s) {
        // Base64 URL without padding; add '=' to make length % 4 == 0
        int mod = s.length() % 4;
        if (mod == 2) return s + "==";
        if (mod == 3) return s + "=";
        if (mod == 1) return s + "==="; // rare/malformed, still try
        return s;
    }

    private static JsonObject safeParse(String json) {
        try {
            if (json == null || json.isBlank()) return new JsonObject();
            JsonElement elem = JsonParser.parseString(json);
            if (elem != null && elem.isJsonObject()) {
                return elem.getAsJsonObject();
            }
        } catch (Exception ignored) { }
        return new JsonObject();
    }
}
