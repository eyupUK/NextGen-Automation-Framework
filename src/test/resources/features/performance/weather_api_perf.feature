@performance @api @weather
Feature: Weather API Performance Tests
  As a performance tester
  I want to run performance tests against the Weather API
  So that I can evaluate its responsiveness under load

    # Acceptance Criteria
    # - Performance tests execute without unhandled exceptions
    # - Basic response success for Weather API calls (requires WEATHER_API_KEY)
    # - Console shows a brief performance report after test execution

  Background:
    Given weather api is ready for performance tests

  @smoke
  Scenario: Run load example against Weather API
    When I run the load performance for Weather API
    Then it completes successfully performance testing of weather api

  Scenario: Run stress example against Weather API
    When I run the stress performance for Weather API
    Then it completes successfully performance testing of weather api

  Scenario: Run spike example against Weather API
    When I run the spike performance for Weather API
    Then it completes successfully performance testing of weather api

  Scenario: Run endurance example against Weather API
    When I run the endurance performance for Weather API
    Then it completes successfully performance testing of weather api
