@security @oauth @api
Feature: OAuth 2.0 security checks

  As a developer integrating OAuth-protected services
  I want to obtain and use access tokens via client credentials
  So that my service can call protected resources securely

  # Acceptance Criteria
  # - Requesting a token via client credentials grant returns a Bearer access token.
  # - If the token is a JWT, it contains a header with an algorithm and a valid JSON payload.
  # - Calling a protected resource with the token returns a successful 2xx response.
  # - Missing/invalid credentials produce a clear error (when applicable in environment).
  #
  # Technical Requirements
  # - OAuth 2.0 token endpoint URL and client credentials are configured via environment or system properties.
  # - HTTPS endpoints are used; secrets (client secret/token) are not logged.
  # - Authorization header uses the appropriate scheme (Basic for token request if required; Bearer for resource).
  # - Retry/backoff is used if rate-limited; time sync considered for JWT validation if needed.

  Validate that OAuth client credentials can obtain an access token and use it to call a protected resource.

  Background:
    Given OAuth 2.0 client credentials are configured

  Scenario: Obtain an access token via client credentials grant
    When I request an OAuth access token
    Then I receive an access token of type Bearer
    And if the access token is a JWT it has a header algorithm and a JSON payload

  Scenario: Use the access token to call a protected resource
    Given I have an OAuth access token
    When I call the OAuth probe endpoint with the token
    Then the response status is a successful 2xx
