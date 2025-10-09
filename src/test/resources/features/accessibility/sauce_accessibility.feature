@accessibility
Feature: Accessibility testing for SauceDemo
  As a quality engineer
  I want to ensure key pages of SauceDemo meet accessibility standards
  So that users with disabilities can use the site effectively

  @login
  Scenario: Login page has no serious or critical accessibility violations
    Given I open the SauceDemo login page
    When I scan the page for accessibility issues
    Then there should be no serious or critical accessibility violations

  @inventory
  Scenario: Inventory page has no serious or critical accessibility violations
    Given I login to SauceDemo as standard user
    When I scan the page for accessibility issues
    Then there should be no serious or critical accessibility violations

  # New: Cart page
  @cart
  Scenario: Cart page has no serious or critical accessibility violations
    Given I login to SauceDemo as standard user
    And I add the product "Sauce Labs Backpack" to the cart
    And I open the cart page
    When I scan the page for accessibility issues
    Then there should be no serious or critical accessibility violations

  # New: Checkout Information page
  @checkout_info
  Scenario: Checkout information page has no serious or critical accessibility violations
    Given I login to SauceDemo as standard user
    And I add the product "Sauce Labs Backpack" to the cart
    And I open the cart page
    And I proceed to checkout information page
    When I scan the page for accessibility issues
    Then there should be no serious or critical accessibility violations

  # New: Checkout Overview page
  @checkout_overview
  Scenario: Checkout overview page has no serious or critical accessibility violations
    Given I login to SauceDemo as standard user
    And I add the product "Sauce Labs Backpack" to the cart
    And I open the cart page
    And I proceed to checkout information page
    And I fill the checkout form with valid data
    When I scan the page for accessibility issues
    Then there should be no serious or critical accessibility violations

  # New: Checkout Complete page
  @checkout_complete
  Scenario: Checkout complete page has no serious or critical accessibility violations
    Given I login to SauceDemo as standard user
    And I add the product "Sauce Labs Backpack" to the cart
    And I open the cart page
    And I proceed to checkout information page
    And I fill the checkout form with valid data
    And I finish the checkout
    When I scan the page for accessibility issues
    Then there should be no serious or critical accessibility violations

  # New: Login error state
  @login_error
  Scenario: Login error state has no serious or critical accessibility violations
    Given I open the SauceDemo login page
    And I try to login with invalid credentials
    When I scan the page for accessibility issues
    Then there should be no serious or critical accessibility violations
