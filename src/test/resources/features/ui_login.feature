@ui @login
Feature: Login

  As a user of SauceDemo
  I want to login
  So that I can view products

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
