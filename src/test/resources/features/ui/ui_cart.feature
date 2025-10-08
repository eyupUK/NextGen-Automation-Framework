@ui @cart
Feature: Cart

  As a logged in shopper
  I want to add items to my cart
  So that I can purchase them

  Background:
    Given I am logged in as a standard user

  Scenario: Add a single product to cart and verify
    When I add the product "Sauce Labs Backpack" to the cart
    Then the cart badge should show 1
    When I open the cart
    Then the cart should contain "Sauce Labs Backpack"
