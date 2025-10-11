@performance @api @fakestore
Feature: Fake Store API Performance Tests
  As a performance tester
  I want to run performance tests against the Fake Store API
  So that I can evaluate its responsiveness under load

    # Acceptance Criteria
    # - Performance tests execute without unhandled exceptions
    # - Basic response success for Fake Store API calls
    # - Console shows a brief performance report after test execution

  Background:
    Given fake store api is ready for performance tests

  @smoke
  Scenario: Run load test against Products of Fake Store API
    When I run the load performance for Products endpoint
    Then it completes successfully performance testing of fake store api

  @smoke
  Scenario: Run load test against Product Detail of Fake Store API
    When I run the load performance for Product Detail endpoint
    Then it completes successfully performance testing of fake store api
