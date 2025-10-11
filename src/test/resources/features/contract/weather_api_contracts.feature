@contract @api
Feature: Weather API Contracts
  As a consumer of the Weather API
  I want to verify consumer-driven contracts for key scenarios
  So that integrations remain stable

  @contract
  Scenario Outline: Verify consumer contract via Pact JUnit test
    Given the Weather API consumer contracts are available
    When I run the contract test "<testClass>"
    Then the contract verification should pass

    Examples:
      | testClass                          |
      | WeatherApiConsumerPactTest         |
      | WeatherApiCoordinatesPactTest      |
      | WeatherApiZipCodePactTest          |
      | WeatherApiForecastPactTest         |
      | WeatherApiErrorsPactTest           |
      | WeatherApiInvalidLocationPactTest  |
      | WeatherApiBulkRequestPactTest      |
