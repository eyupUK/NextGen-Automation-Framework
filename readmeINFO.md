# Java-Cucumber-Selenium-Rest Assured Framework

This repository provides an automation framework using Selenium, Cucumber, Rest Assured and Java for web application and API testing. It demonstrates BDD with Cucumber, integrates Selenium for UI automation, and supports API testing for domains like SauceLabs, Fake API Store, and weather APIs.

## Prerequisites

- JDK 21 (LTS)  
  Ensure `JAVA_HOME` is set to `/Users/mac/Library/Java/JavaVirtualMachines/openjdk-21/Contents/Home`
- Maven  
  Ensure Maven is installed and available in your `PATH`
- IntelliJ IDEA (recommended) or Eclipse with Maven, Cucumber, and Git plugins
- Allure CLI (optional, for advanced reporting)

## Features

- **Selenium Integration**: Automates web UI interactions.
- **Cucumber BDD**: Write tests in Gherkin syntax for better collaboration.
- **Java**: Robust, maintainable test code.
- **Dynamic Data Testing**: Supports parameterized and data-driven tests.
- **Error Handling**: Captures screenshots and logs on failures.
- **Reporting**: Generates Surefire, Cucumber, HTML, and JSON reports. Allure integration for advanced reporting.
- **Configuration Management**: Uses `configuration.properties` for environment and credential management.
- **Utilities**: Helpers for UI, API, Excel, and more.
- **Page Object Model**: Clean separation of UI logic.
- **Cross-Browser Support**: Run tests on Chrome, Firefox, etc. (local ChromeDriver required for local runs).
- **Synchronization**: Uses explicit waits for reliable UI automation.
- **Parallel Execution**: Supports parallel test execution via Maven Surefire plugin.
- **API Testing**: Includes tests for various public APIs including JSON schema validation.
- **CI/CD Ready**: Easily integrates with Jenkins, GitHub Actions, etc.
- **Data Driven Testing**: Supports CSV and JSON data sources.

## Project Structure

- `pom.xml` — Maven build and dependency management
- `src/test/java` — Step definitions, hooks, runners, page objects, utilities
- `src/test/resources` — Feature files, configuration
- `configuration.properties` — Environment and credential settings
- `target/` — Build output, reports
- `allure-results/` — Allure report data
- `cucumber-report.html` — Cucumber HTML report

## Setup

1. Clone the repository:
   ```sh
   git clone https://github.com/insurwave/code-test-Eyup-Tozcu.git
   cd qa-assessment-Eyup-Tozcu
    ```
2. Open in IntelliJ IDEA or Eclipse.
3. Ensure JDK 21 and Maven are configured.
4. Update `configuration.properties` with the target URL and credentials if needed.
5. Install Allure CLI if advanced reporting is needed.
6. Run tests using Maven commands
    ```sh
    mvn verify
    ```
7. View reports in `target/cucumber-report.html` or generate Allure reports.
    ```sh
    allure serve
    ```
8. Review test results and logs in the `target` directory.

### Key Components
- **pom.xml file**: Manages dependencies and builds required for the framework.
- **Feature File**: Specifies the steps in BDD language style for Selenium tests.
- **Hooks class**: Sets up preconditions and cleans up after tests.
- **Step Definition Classes**: Contains automation test code that corresponds to the steps defined in the feature files.
- **Page Object Class**: Models UI areas as objects within the test code to reduce duplication and maintenance.
- **Utilities**: Helper classes for common tasks like reading properties, handling Excel files, and managing WebDriver instances.
- **Cucumber Runner Class**: Configures and runs Cucumber tests.
- **Configuration Properties File**: Stores environment-specific settings and credentials.
- **Test Data and Schemas**: Stored in .csv or JSON files for API testing under resource.

## Running Tests
To run scenarios, use the following ways: 
- Run CukesRunner by tagging desired scenarios or all scenarios without any tag.
- Run specific scenarios by tagging them in the feature file.
- Run specific scenarios by using Run Test button in the feature file
- Run specific scenarios by using the cucumber.options parameter in the command line.
- Navigate to the project directory and run `mvn verify` or `mvn test` to execute tests. Specify browsers with `-Dbrowser=browser_name`. Use `mvn test -Dcucumber.options="classpath:features/my_first.feature"` to run specific features.
- **Parallel** execution is supported via Maven Surefire plugin configuration.

## Reporting
Generates automatically Surfire, Cucumber, HTML, and JSON reports. For allure reports, use `allure generate --clean` or `allure serve` for immediate viewing.
![Report1](images/allure1.png)

![Report2](images/allure2.png)

![Report3](images/cucumber.png)
## Note
This framework does not include sensitive data or browser drivers. Ensure to provide URL in `configuration.properties` and credentials in the feature file.