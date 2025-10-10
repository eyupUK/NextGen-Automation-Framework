@performance
Feature: Performance smoke checks

  As a performance engineer
  I want to run small-load smoke tests
  So that I can quickly validate API responsiveness

  # Acceptance Criteria
  # - Examples execute without unhandled exceptions
  # - Basic response success for Weather API calls (requires WEATHER_API_KEY)
  # - Console shows a brief performance report

  Background:
    Given performance examples are available

  @smoke
  Scenario: Run simple load example
    When I run the simple load performance example
    Then it completes successfully

  Scenario: Run ramp-up example
    When I run the ramp-up performance example
    Then it completes successfully
