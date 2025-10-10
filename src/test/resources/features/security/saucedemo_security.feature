@security @web
Feature: SauceDemo.com security checks

  As a security-focused web tester
  I want to validate security headers and mixed-content policies on the homepage
  So that users are protected against common web threats

  # Acceptance Criteria
  # - Homepage response includes Strict-Transport-Security, X-Content-Type-Options=nosniff, and a Content-Security-Policy header.
  # - The response has either X-Frame-Options or a CSP frame-ancestors directive.
  # - The homepage HTML contains no insecure http:// references (no mixed content).
  # - CSP must not contain unsafe-inline or unsafe-eval directives.
  # - All checks are performed over HTTPS and return successful responses before assertions.
  #
  # Technical Requirements
  # - Public site https://www.saucedemo.com is reachable from the test environment.
  # - HTTP client follows redirects and captures response headers and body for assertions.
  # - OWASP baseline checks are implemented in step definitions (no external calls required).
  # - Secrets are not logged; test output artifacts are written under target/.

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
