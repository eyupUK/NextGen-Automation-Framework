@api @forecast
Feature: Forecast

  As a developer integrating with the Weather API
  I want to retrieve multi-day forecast data for different location formats
  So that I can display accurate upcoming weather information to users

  # Acceptance Criteria
  # - For each request, the API returns HTTP 200 for valid queries and day counts.
  # - The response conforms to schemas/forecast_schema.json.
  # - The payload contains exactly <days> forecast entries (array length matches requested days).
  # - CSV scenario completes successfully and the last call status is 200.
  #
  # Technical Requirements
  # - weather_api_base_url configured and reachable.
  # - WEATHER_API_KEY provided via environment variable or system property.
  # - Schema files available under src/test/resources/schemas/.
  # - Respect provider rate limits when running multiple requests rapidly.

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
