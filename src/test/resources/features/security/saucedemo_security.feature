@security @web
Feature: SauceDemo.com security checks
  Validate important security headers and no mixed content on the public homepage.

  Scenario: Homepage returns recommended security headers
    Given I query the SauceDemo homepage
    Then the web response header "Strict-Transport-Security" is present
    And the web response header "X-Content-Type-Options" equals "nosniff"
    And the web response header "Content-Security-Policy" is present
    And the response has either "X-Frame-Options" or a CSP "frame-ancestors" directive
    And the response meets OWASP baseline for HTTPS endpoints

  Scenario: Homepage HTML does not reference insecure resources
    Given I fetch the SauceDemo homepage HTML
    Then the page should not reference insecure http resources

  Scenario: CSP should not allow unsafe directives
    Given I query the SauceDemo homepage
    Then the CSP must not contain "unsafe-inline" or "unsafe-eval"
