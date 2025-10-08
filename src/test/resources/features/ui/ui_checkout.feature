@ui @checkout
Feature: Checkout

  As a shopper
  I want to complete a purchase
  So that I can receive my items

  Background:
    Given I am logged in as a standard user

  Scenario: Complete a checkout flow
    When I add "Sauce Labs Backpack" to my cart and start checkout
    And I provide checkout details "John" , "Doe" , "AB12CD" and continue
    And I finish the checkout
    Then I should see the order completion page
