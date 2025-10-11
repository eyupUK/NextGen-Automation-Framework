@api @errors
Feature: Bulk and Error Handling of Weather API

  As a Weather API consumer
  I want clear error responses and defined bulk request behavior
  So that clients can handle failures predictably and respect plan limits

  # Acceptance Criteria
  # - Posting a bulk current request on the free plan returns HTTP 400 with error code 2009 and an explanatory message.
  # - Omitting the required "q" parameter returns HTTP 400 with error code 1003 and a descriptive message.
  # - Requesting an unknown location returns HTTP 400 with error code 1006 and a descriptive message.
  # - Error responses conform to schemas/error_schema.json.
  # - No scenario should result in a server error (5xx) in these negative cases.
  #
  # Technical Requirements
  # - Weather API base URL and API key are configured via environment or system properties.
  # - JSON schemas are available under src/test/resources/schemas/.
  # - Tests respect provider rate limits (introduce delays if needed).
  # - Network calls are made over HTTPS; secrets are not committed to source control.

  Background:
    Given I have a valid WeatherAPI key configured

  # Demonstrates POST and graceful handling (free plan should be 400, code 2009)
  Scenario: POST bulk current on free plan returns forbidden
    When I POST a bulk current request
    Then the response status is 400
    And the response matches schema "schemas/error_schema.json"
    And the error code is 2009 and message contains "does not have access"

  Scenario: Missing q parameter returns 400 with specific error code
    When I request current weather with no query parameter
    Then the response status is 400
    And the response matches schema "schemas/error_schema.json"
    And the error code is 1003 and message contains "Parameter q is missing"

  Scenario: Unknown location returns 400 with specific error code
    When I request current weather for an unknown location
    Then the response status is 400
    And the response matches schema "schemas/error_schema.json"
    And the error code is 1006 and message contains "No location found matching parameter 'q'"