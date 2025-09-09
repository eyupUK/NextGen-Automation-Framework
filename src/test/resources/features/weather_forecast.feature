@api @forecast
Feature: Forecast

  Background:
    Given I have a valid WeatherAPI key configured

  Scenario Outline: Get N-day forecast and verify array length and types
    When I request a <days>-day forecast for "<query>"
    Then the response status is 200
    And the response matches schema "schemas/forecast_schema.json"
    And the payload contains exactly the requested number of forecast days

    Examples:
      | query          | days |
      | London         | 3    |
      | 90201          | 2    |
      | 48.8567,2.3508 | 1    |

  @csv
  Scenario: Forecast for many cities from CSV
    Given test cities are loaded from "cities.csv"
    When I request a forecast for each city in "cities.csv"
    Then the response status is 200
