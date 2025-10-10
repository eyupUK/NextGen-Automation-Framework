package com.example.util;

public final class OAuthConfig {

    private OAuthConfig() {}

    private static String envKeyFor(String dottedKey) {
        // Convert e.g., oauth.token_url -> OAUTH_TOKEN_URL
        return dottedKey.replace('.', '_').replace('-', '_').toUpperCase();
    }

    public static String get(String dottedKey) {
        // 1) Prefer environment variable in OAUTH_* style
        String env = System.getenv(envKeyFor(dottedKey));
        if (env != null && !env.isBlank()) {
            return env;
        }
        // 2) Next prefer exact system property (e.g., -Doauth.token_url=...)
        String sys = System.getProperty(dottedKey);
        if (sys != null && !sys.isBlank()) {
            return sys;
        }
        // 3) Finally fall back to configuration.properties via ConfigurationReader
        return ConfigurationReader.get(dottedKey);
    }

    public static String tokenUrl() { return get("oauth.token_url"); }
    public static String clientId() { return get("oauth.client_id"); }
    public static String clientSecret() { return get("oauth.client_secret"); }
    public static String scope() { return get("oauth.scope"); }
    public static String probeUrl() { return get("oauth.probe_url"); }
}

