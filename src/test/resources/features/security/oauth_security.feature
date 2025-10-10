@security @oauth
Feature: OAuth 2.0 security checks
  Validate that OAuth client credentials can obtain an access token and use it to call a protected resource.

  Background:
    Given OAuth 2.0 client credentials are configured

  Scenario: Obtain an access token via client credentials grant
    When I request an OAuth access token
    Then I receive an access token of type Bearer
    And if the access token is a JWT it has a header algorithm and a JSON payload

  Scenario: Use the access token to call a protected resource
    Given I have an OAuth access token
    When I call the OAuth probe endpoint with the token
    Then the response status is a successful 2xx

