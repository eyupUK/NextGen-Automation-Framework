@security @api
Feature: Weather API security checks
  Ensure the Weather API enforces authentication, resists simple injection attempts, and returns security headers.

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
      | 🙂                           |

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
    When I rapidly call current weather 25 times with a valid API key
    Then I eventually receive a 429 status
    And the response header "Retry-After" is present
    When I wait the Retry-After duration then retry the request
    Then the next response status is not 429
