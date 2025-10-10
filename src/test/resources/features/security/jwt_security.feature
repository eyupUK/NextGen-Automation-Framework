@security @jwt @api
Feature: Public Demo JWT API security checks (EscuelaJS)

  As a security tester of public demo APIs
  I want to verify JWT issuance and protected endpoint access
  So that authentication flows are validated end-to-end

  # Acceptance Criteria
  # - Registering and authenticating returns a JWT bearer token.
  # - The JWT header contains an algorithm and the JWT payload is valid JSON.
  # - Calling a protected endpoint with the token returns HTTP 200.
  # - Authenticating with an incorrect password returns an error status.
  # - Calling a protected endpoint with a tampered or invalid token returns 401 or 403.
  #
  # Technical Requirements
  # - Demo JWT API base URL is configured and reachable from the test environment.
  # - User credentials for the demo are generated or provided via configuration; secrets are not logged.
  # - HTTPS endpoints are used; JWT parsing avoids logging sensitive claims.
  # - Requests include Authorization: Bearer <token> header; retry/backoff is used if rate-limited.

  Validate that JWT authentication returns a signed token with standard structure and it can be used in requests.

  Background:
    Given the Demo JWT API base is configured

  Scenario: Obtain a JWT token by registering and authenticating
    When I register a demo user and authenticate
    Then I receive a JWT bearer token
    And the JWT header includes an algorithm
    And the JWT payload is a valid JSON object

  Scenario: Use the JWT token in an API request
    Given I have a JWT bearer token
    When I call a protected demo endpoint with the token
    Then the response status code is 200

  @negative
  Scenario: Authentication fails with incorrect password
    When I ensure a demo user is registered
    And I authenticate with an incorrect password
    Then the token endpoint returns an error status

  @negative
  Scenario: Protected endpoint rejects tampered (invalid/expired-like) token
    Given I have a JWT bearer token
    When I call a protected demo endpoint with a tampered token
    Then the response status is one of 401 or 403
