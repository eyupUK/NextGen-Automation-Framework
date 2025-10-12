@security @api
Feature: Weather API Security Checks

  As a security-conscious API tester
  I want to verify authentication, injection resilience, and security headers
  So that consumers are protected and best practices are enforced

  # Acceptance Criteria
  # - Requests without a valid API key do not return HTTP 200 and provide an error payload with code and message.
  # - Injection-like payloads (SQL/JS/control characters/emojis) do not cause server errors (no 5xx).
  # - Responses expose baseline security headers (e.g., X-Content-Type-Options=nosniff; HSTS when applicable).
  # - CORS preflight honors allowed origins/methods/headers and does not echo disallowed origins.
  # - Rapid requests eventually yield 429 with Retry-After, and retrying after the duration succeeds.
  # - All schema validations (where applicable) pass for error payloads.
  #
  # Technical Requirements
  # - Weather API base URL and API key are provided via configuration (env/system properties).
  # - HTTPS endpoints are used; secrets are not logged or committed.
  # - CORS preflight tests can send OPTIONS requests with custom headers.
  # - JSON schema files are available under src/test/resources/schemas/.
  # - Rate limiting respected in tests (add waits/backoff when necessary).

  # Ensure the Weather API enforces authentication, resists simple injection attempts, and returns security headers.

  Background:
    Given the Weather API base is configured

  Scenario: Weather API requires a valid API key
    When I call current weather with an invalid API key
    Then the response status is not 200
    And the error payload contains a code and message

  @requires_key
  Scenario Outline: Injection-like payloads must not cause server errors
    When I request current weather for "<payload>" with a valid API key
    Then the response status is not a server error
    Examples:
      | payload                     |
      | ' OR '1'='1                 |
      | <script>alert(1)</script>   |
      | London; DROP TABLE users;   |
      | %27%20OR%201=1--            |
      | \ud83d\ude42                           |

  @requires_key
  Scenario: API response exposes basic security headers
    When I request current weather for "London" with a valid API key
    Then the response content type is JSON
    And the response header "X-Content-Type-Options" equals "nosniff"
    And the response header "Strict-Transport-Security" is present
    And the response meets OWASP API baseline for HTTPS endpoints

  # Optional: Use this if provider omits HSTS for JSON endpoints
  @requires_key @no_hsts
  Scenario: API baseline without requiring HSTS
    When I request current weather for "London" with a valid API key
    Then the response meets OWASP API baseline for HTTPS endpoints without HSTS

  # CORS validation (preflight) - configurable
  @requires_key
  Scenario Outline: CORS preflight allows <method> for <origin> with requested headers <headers>
    When I preflight current weather CORS for origin "<origin>" method "<method>" and request headers "<headers>"
    Then the response header "Access-Control-Allow-Origin" equals "<origin>"
    And the response header "Access-Control-Allow-Methods" contains "<method>"
    And the response header "Access-Control-Allow-Headers" contains all of "<headers>"
    Examples:
      | origin                 | method | headers                      |
      | https://example.com    | GET    | Content-Type, Accept         |
      | https://myapp.example  | POST   | Content-Type, X-Custom-Auth  |

  # Negative CORS case: disallowed origin should not be echoed
  @requires_key
  Scenario: CORS preflight disallowed origin is not allowed
    When I preflight current weather CORS for origin "https://evil.invalid" method "GET" and request headers "Content-Type"
    Then the response should not allow origin "https://evil.invalid"

  # Rate-limiting behavior (may be environment dependent)
  @requires_key @rate_limit
  Scenario: Rapid calls eventually yield 429 with Retry-After and backoff honored
    When I rapidly call current weather 50 times with a valid API key
    Then I eventually receive a 429 status
    And the response header "Retry-After" is present
    When I wait the Retry-After duration then retry the request
    Then the next response status is not 429
