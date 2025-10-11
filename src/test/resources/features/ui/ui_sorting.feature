@ui @sorting
Feature: Product Sorting on SauceDemo

  As a shopper
  I want to sort products
  So that I can find items easily

  # Acceptance Criteria
  # - Sorting by "Price (low to high)" displays product prices in ascending order.
  # - Sorting by "Name (Z to A)" displays product names in descending alphabetical order.
  # - Pages under test load successfully before assertions (HTTP 200 + UI ready).
  # - Sorting actions use the site’s sort control and reflect immediately in the product list.
  #
  # Technical Requirements
  # - SauceDemo base URL is configured and reachable from the test environment.
  # - Standard user credentials are available via configuration.
  # - Chrome WebDriver is used with a compatible driver version; headless mode supported.
  # - Stable selectors (data-test or semantic locators) are used for sort control and product items.

  Background:
    Given I am logged in as a standard user

  Scenario: Sort by Price (low to high)
    When I sort products by "Price (low to high)"
    Then product prices should be in ascending order

  Scenario: Sort by Name (Z to A)
    When I sort products by "Name (Z to A)"
    Then product names should be in descending order
