@api @current
Feature: Current weather

  As a developer integrating with the Weather API
  I want to fetch current weather by city, zip/postcode, or coordinates
  So that users can see up-to-date local conditions

  # Acceptance Criteria
  # - For valid queries (city, zip/postcode, lat,long), the API returns HTTP 200.
  # - The response conforms to schemas/current_schema.json and typed checks pass.
  # - Optional country assertion matches when provided in examples; otherwise is skipped.
  # - CSV-driven scenario loads all cities and each call succeeds; final call status is 200.
  #
  # Technical Requirements
  # - weather_api_base_url configured and reachable from test environment.
  # - WEATHER_API_KEY supplied via env var or system prop; secrets not committed.
  # - Schema files are present under src/test/resources/schemas/.
  # - Respect rate limits when running large CSV sets (use throttling if needed).

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
