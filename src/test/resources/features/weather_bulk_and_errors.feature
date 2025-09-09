@api @errors
Feature: Bulk and error handling

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