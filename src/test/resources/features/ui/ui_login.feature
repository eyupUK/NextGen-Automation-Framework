@ui @login
Feature: Login to SauceDemo

  As a user of SauceDemo
  I want to login
  So that I can view products

  # Acceptance Criteria
  # - Successful login with valid credentials navigates to the products page.
  # - Invalid credentials display an error message matching the scenario examples.
  # - Login page loads successfully before actions and elements are interactable.
  # - Error state is visible on failure and does not persist after a successful login.
  #
  # Technical Requirements
  # - SauceDemo base URL is configured and reachable from the test environment.
  # - Standard/locked-out credentials provided via configuration variables.
  # - Chrome WebDriver (headed/headless) is used with a compatible driver version.
  # - Stable selectors are used for username, password, login button, and error message elements.

  Background:
    Given I am on the SauceDemo login page

  Scenario Outline: Successful login
    When I login with username "<username>" and password "<password>"
    Then I should see the products page


    Examples:
      | username       | password     |
      | standard_user  | secret_sauce |

  Scenario Outline: Invalid login shows an error
    When I login with username "<username>" and password "<password>"
    Then I should see an error message containing "<message>"

    Examples:
      | username       | password     | message                              |
      | locked_out_user| secret_sauce | Epic sadface: Sorry, this user has been locked out. |
      | standard_user  | wrong_pass   | Epic sadface: Username and password do not match     |
