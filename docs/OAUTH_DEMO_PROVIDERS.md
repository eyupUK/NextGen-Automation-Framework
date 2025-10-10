# OAuth Demo Providers

This project now includes a real-world OAuth 2.0 demo provider you can use to practice the client credentials flow end-to-end.

## Option 1: Duende IdentityServer (Public Demo)

- Token endpoint: `https://demo.duendesoftware.com/connect/token`
- Client ID: `m2m.short`
- Client Secret: `secret`
- Scope: `api`
- Protected API probe: `https://demo.duendesoftware.com/api/test`

These values are pre-populated in `configuration.properties` via the following keys:

```
oauth.token_url=https://demo.duendesoftware.com/connect/token
oauth.client_id=m2m.short
oauth.client_secret=secret
oauth.scope=api
oauth.probe_url=https://demo.duendesoftware.com/api/test
```

Override via environment variables (recommended for secrets):

```zsh
export OAUTH_TOKEN_URL="https://demo.duendesoftware.com/connect/token"
export OAUTH_CLIENT_ID="m2m.short"
export OAUTH_CLIENT_SECRET="secret"   # demo secret – don’t reuse for anything else
export OAUTH_SCOPE="api"
export OAUTH_PROBE_URL="https://demo.duendesoftware.com/api/test"
```

Quick validation with cURL:

```zsh
# 1) Get a token (client credentials)
ACCESS_TOKEN=$(\
  curl -s -X POST "$OAUTH_TOKEN_URL" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -u "$OAUTH_CLIENT_ID:$OAUTH_CLIENT_SECRET" \
  -d "grant_type=client_credentials&scope=$OAUTH_SCOPE" | jq -r .access_token)

# 2) Call the protected API using the token
curl -i "$OAUTH_PROBE_URL" -H "Authorization: Bearer $ACCESS_TOKEN"
```

If the token is valid, the API should return `200 OK` and a small JSON payload.

Notes and best practices:
- Always use HTTPS.
- Do not log client secrets or full tokens in CI.
- Respect demo provider limits; add small backoffs in loops.

## Option 2: Spotify Client Credentials (Real Provider)

Register a developer app at https://developer.spotify.com/dashboard and capture your Client ID and Client Secret.

- Token endpoint: `https://accounts.spotify.com/api/token`
- Scope: leave blank for basic app-only calls
- Example probe: `https://api.spotify.com/v1/search?q=daft%20punk&type=artist&limit=1`

Set environment variables:

```zsh
export OAUTH_TOKEN_URL="https://accounts.spotify.com/api/token"
export OAUTH_CLIENT_ID="YOUR_SPOTIFY_CLIENT_ID"
export OAUTH_CLIENT_SECRET="YOUR_SPOTIFY_CLIENT_SECRET"
export OAUTH_SCOPE=""   # often blank for client credentials
export OAUTH_PROBE_URL="https://api.spotify.com/v1/search?q=daft%20punk&type=artist&limit=1"
```

cURL smoke test:

```zsh
ACCESS_TOKEN=$(\
  curl -s -X POST "$OAUTH_TOKEN_URL" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -u "$OAUTH_CLIENT_ID:$OAUTH_CLIENT_SECRET" \
  -d 'grant_type=client_credentials' | jq -r .access_token)

curl -i "$OAUTH_PROBE_URL" -H "Authorization: Bearer $ACCESS_TOKEN"
```

If configured properly, you should receive a `200 OK` with JSON matching Spotify’s API schema.

## Running the OAuth Scenarios

- All OAuth scenarios (Duende by default unless you override with `OAUTH_*`):
```zsh
mvn clean test -Dtest=SecurityRunner -Dcucumber.filter.tags='@oauth'
```

- Spotify-only scenarios (opaque token path via `@spotify`):
```zsh
mvn clean test -Dtest=SecurityRunner -Dcucumber.filter.tags='@spotify'
```

> Note: The `@spotify` opaque-token assertion is conditionally skipped if your configured `OAUTH_TOKEN_URL` does not point to Spotify (`accounts.spotify.com`). This prevents false failures when using JWT providers like Duende.

## Option 3: Local Keycloak (Advanced)

For a closer-to-production experience, you can run a local Keycloak via Docker and configure a client credentials flow. This requires a bit of setup (realm, client, scope, and a protected resource). Consider this when you want to simulate custom token lifetimes, realms, and client policies.

High-level steps:
1. Run Keycloak in Docker with an admin user.
2. Create a realm and a confidential client with `client_credentials` enabled.
3. Expose a simple protected resource (e.g., a local HTTP service) and validate Bearer tokens.

Because this is more involved and environment-specific, we’ve left it as an optional exercise.

---

Once configured, the existing `oauth_security.feature` scenarios will:
- Request an OAuth access token (client credentials).
- Optionally validate JWT structure.
- Call a protected probe endpoint using the Bearer token.

Make sure the step definitions read configuration from environment variables or `configuration.properties` for the keys listed above.
