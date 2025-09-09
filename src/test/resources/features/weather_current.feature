@api @current
Feature: Current weather

  Background:
    Given I have a valid WeatherAPI key configured

  Scenario Outline: Get current weather for a single city (typed checks + optional country)
    When I request current weather for "<query>"
    Then the response status is 200
    And the payload has valid current weather types
    And the response matches schema "schemas/current_schema.json"
    And the "location.country" equals "<expectedCountry>" if provided

    Examples:
      | query          | expectedCountry |
      | London         | United Kingdom  |
      | 90201          | USA             |
      | 48.8567,2.3508 | France          |
      | SW1            | UK              |

  @csv
  Scenario: Get current weather for multiple cities from CSV
    Given test cities are loaded from "cities.csv"
    When I request current weather for each city in "cities.csv"
    Then the response status is 200
    # status validated per row in step; this step asserts the final call is 200

