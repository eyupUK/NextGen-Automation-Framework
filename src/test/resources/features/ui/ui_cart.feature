@ui @cart
Feature: Cart

  As a logged in shopper
  I want to add items to my cart
  So that I can purchase them

  # Acceptance Criteria
  # - Adding a product updates the cart badge to the correct item count.
  # - The cart page lists added items with correct name and price.
  # - Cart contents persist for the session until cleared or checkout begins.
  # - Removing an item (when implemented) reduces the badge and removes the item from the cart page.
  #
  # Technical Requirements
  # - SauceDemo base URL is configured and reachable from the test environment.
  # - Standard user credentials are provided via configuration variables.
  # - Chrome WebDriver runs in headed or headless mode with a compatible driver.
  # - Stable selectors (data-test/aria-label) are used for add-to-cart buttons, cart badge, and cart items.

  Background:
    Given I am logged in as a standard user

  Scenario: Add a single product to cart and verify
    When I add the product "Sauce Labs Backpack" to the cart
    Then the cart badge should show 1
    When I open the cart
    Then the cart should contain "Sauce Labs Backpack"
