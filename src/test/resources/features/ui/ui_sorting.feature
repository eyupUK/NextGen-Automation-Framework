@ui @sorting
Feature: Product sorting

  As a shopper
  I want to sort products
  So that I can find items easily

  Background:
    Given I am logged in as a standard user

  Scenario: Sort by Price (low to high)
    When I sort products by "Price (low to high)"
    Then product prices should be in ascending order

  Scenario: Sort by Name (Z to A)
    When I sort products by "Name (Z to A)"
    Then product names should be in descending order
