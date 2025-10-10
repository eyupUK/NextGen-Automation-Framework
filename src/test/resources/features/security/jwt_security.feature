@security @jwt
Feature: FakeStore JWT security checks
  Validate that JWT authentication returns a signed token with standard structure and it can be used in requests.

  Background:
    Given the FakeStore API base is configured

  Scenario: Obtain a JWT token with configured credentials
    When I authenticate to FakeStore with configured credentials
    Then I receive a JWT bearer token
    And the JWT header includes an algorithm
    And the JWT payload is a valid JSON object

  Scenario: Use the JWT token in an API request
    Given I have a JWT bearer token from FakeStore
    When I call a FakeStore endpoint with the token
    Then the response status is 200

