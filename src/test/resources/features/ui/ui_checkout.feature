@ui @checkout
Feature: Checkout Flow on SauceDemo

  As a shopper
  I want to complete a purchase
  So that I can receive my items

  # Acceptance Criteria
  # - Completing checkout with valid first name, last name, and postal code shows the order completion page.
  # - Checkout overview lists the selected items and price summary before finishing.
  # - After finishing, the confirmation message is visible and the cart is cleared.
  # - Pages under test load successfully before assertions (HTTP 200 + UI ready).
  #
  # Technical Requirements
  # - SauceDemo base URL is configured and reachable from the test environment.
  # - Standard user credentials are available via configuration.
  # - Chrome WebDriver is used with a compatible driver version; headless mode supported.
  # - Stable selectors (data-test or semantic locators) are used for checkout form fields and buttons.

  Background:
    Given I am logged in as a standard user

  Scenario: Complete a checkout flow
    When I add "Sauce Labs Backpack" to my cart and start checkout
    And I provide checkout details "John" , "Doe" , "AB12CD" and continue
    And I finish the checkout
    Then I should see the order completion page
