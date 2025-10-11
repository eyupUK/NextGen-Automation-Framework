# Java • JUnit • Cucumber • Selenium • Rest Assured • aXe • JWT OAuth Owasp  • Gatling • Pact

- Quick Links: [README.md](README.md) (quick start) • [HOW_TO_RUN_PERFORMANCE_TESTS.md](docs/HOW_TO_RUN_PERFORMANCE_TESTS.md) (performance guide)

This repository provides a unified automation framework for UI, API, accessibility, security baseline, and performance testing using Java 21, Maven, Selenium, Cucumber, Rest Assured, axe-core/selenium, and Gatling.

## Prerequisites

- JDK 21
  - Ensure JAVA_HOME points to your JDK 21 install
- Maven 3.9+
- IntelliJ IDEA (recommended)
- Optional: Allure CLI for local report viewing

## Highlights

- Cucumber BDD with page objects and reusable step libs
- UI automation with Selenium WebDriver (Chrome, Firefox, Safari, Edge)
- API testing with Rest Assured (data-driven from CSV/JSON)
- Accessibility checks with axe-core/selenium (@accessibility)
- Security baseline checks for headers on web and API endpoints (@security)
- Parallel execution via Surefire
- Reporting: Cucumber JSON/HTML, PrettyReports, Allure
- Performance testing with Gatling (Weather API, etc.)

## Project Structure

- `pom.xml` — build, dependencies, plugins (Surefire, Allure, Gatling)
- `src/test/java` — runners, step defs, hooks, page objects, utils
- `src/test/resources` — feature files, test data, schemas
- `configuration.properties` — default config (copy from `configuration.properties.template`)
- `target/` — build outputs and reports
  - `target/cucumber-report.html` — Cucumber HTML
  - `target/cucumber.json` — Cucumber JSON
  - `target/allure-results/` — Allure raw results
- `scripts` — helper scripts at repo root:
  - `quick-perf-test.sh`, `run-performance-tests.sh`, `verify-performance-setup.sh`

## Setup

1) Clone and open the project
   ```bash
   git clone <your-repo-url>
   cd <your-repo-directory>
   ```
2) Create your local config
   ```bash
   cp configuration.properties.template configuration.properties
   ```
3) (Optional) Install Allure CLI if you want local Allure HTML reports

## Configuration

- Browser: set in `configuration.properties` or via system property
  - `browser=chrome` (default). Supported: `chrome`, `chrome-headless`, `firefox`, `firefox-headless`, `safari` (macOS), `edge` (Windows)
  - Headless toggle: `-Dheadless=true` (defaults to headless on CI, headed locally)
- SauceDemo URL and default credentials exist in the template file
- Weather API base URL is configured; the API key must be provided via environment
  - `export WEATHER_API_KEY=your_api_key`

## How to Run

- Run all standard tests (excluding @accessibility and @security by default via runner tags)
  ```bash
  mvn clean test
  ```
- Specify browser/headless
  ```bash
  mvn clean test -Dbrowser=chrome-headless
  mvn clean test -Dbrowser=firefox -Dheadless=true
  ```
- Filter by tags (Cucumber v6 system property)
  ```bash
  mvn clean test -Dcucumber.filter.tags="@smoke"
  mvn clean test -Dcucumber.filter.tags="not @accessibility and not @security"
  ```
- Run a specific feature or scenario
  ```bash
  mvn clean test -Dcucumber.features=src/test/resources/features/ui/ui_login.feature
  mvn clean test -Dcucumber.filter.tags="@ui and @login"
  ```

### Runners

- UI/API default: `com.example.runners.CukesRunner`
- Accessibility only: `com.example.runners.AccessibilityCukesRunner` (tags: `@accessibility`)
- Security suite: `com.example.runners.SecurityCukesRunner` (tags: `@security`)
- Performance (JUnit suite): `com.example.runners.PerformanceRunner`
- Performance (Cucumber): `com.example.runners.PerformanceCukesRunner` (default tags: `@performance`)

You can run them from the IDE or with Surefire includes already configured in `pom.xml`.

### Security Test Suite

Covers both web (SauceDemo) and API (Weather API) security headers.

- Useful tags
  - `@security` — all security scenarios
  - `@web` — web-only
  - `@api` — API-only (requires `WEATHER_API_KEY`)
  - `@requires_key` — scenarios needing a valid key
  - `@rate_limit` — rate-limiting tests (usually excluded in CI)
  - `@no_hsts` — relaxed API baseline (skip HSTS for JSON endpoints)
- Examples
  ```bash
  # All security tests
  mvn clean test -Dcucumber.filter.tags='@security'
  
  # Web-only
  mvn clean test -Dcucumber.filter.tags='@security and @web'
  
  # API-only (set key first)
  export WEATHER_API_KEY=your_api_key
  mvn clean test -Dcucumber.filter.tags='@security and @api'
  
  # Exclude rate limit
  mvn clean test -Dcucumber.filter.tags='@security and not @rate_limit'
  
  # Relaxed API baseline without HSTS
  mvn clean test -Dcucumber.filter.tags='@security and @api and @no_hsts'
  ```

### Accessibility

- Tag features/scenarios with `@accessibility` and run via `AccessibilityRunner` or tags:
  ```bash
  mvn clean test -Dcucumber.filter.tags='@accessibility'
  ```

## Performance Testing

### Gatling

Read the step-by-step guide in `HOW_TO_RUN_PERFORMANCE_TESTS.md`. Quick start:

```bash
# Set the Weather API key
export WEATHER_API_KEY=your_api_key

# Quick local run
./quick-perf-test.sh

# Or run directly with Maven
mvn gatling:test \
  -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=5 -Dperf.duration=30
```

### PerformanceRunner (JUnit)

A JUnit suite to run REST Assured-based performance tests (e.g., `WeatherApiPerformanceTest`).

```bash
# Prerequisite
export WEATHER_API_KEY=your_api_key

# Run the JUnit performance suite
mvn -Dtest=PerformanceRunner test

# Optional overrides
mvn -Dtest=PerformanceRunner test \
  -Dperf.users=10 -Dperf.rampup=10 -Dperf.duration=60
```

### Performance (Cucumber)

A Cucumber runner is available for quick perf smoke checks using `@performance` feature(s).

```bash
# Prerequisite for Weather examples
export WEATHER_API_KEY=your_api_key

# Default fast run: executes @performance
mvn -Dtest=PerformanceCukesRunner test

# Run all @performance scenarios (explicit filter)
mvn -Dtest=PerformanceCukesRunner -Dcucumber.filter.tags='@performance' test
```

The starter feature lives at `src/test/resources/features/performance/perf_smoke.feature`.

## Contract Testing (Pact)

Consumer-driven contract testing is supported via Pact JVM (JUnit 4). Pact tests live under `src/test/java/com/example/contract`.

- Provider name: `WeatherAPI`
- Mock server: Pact starts on a free local port (no hardcoded ports); tests use `mockProvider.getUrl()`
- Pact output: `target/pacts/QAFrameworkConsumer-WeatherAPI.json`

### Current test coverage
- Current weather by city: `WeatherApiConsumerPactTest` (London)
- Current weather by coordinates: `WeatherApiCoordinatesPactTest` (48.8567,2.3508 → Paris)
- Current weather by ZIP/postal code: `WeatherApiZipCodePactTest` (90201 → Bell Gardens)
- 3-day forecast: `WeatherApiForecastPactTest` (London)
- Error: missing query parameter: `WeatherApiErrorsPactTest` (400, code 1003)
- Error: invalid/unknown location: `WeatherApiInvalidLocationPactTest` (400, code 1006)
- Error: bulk request not allowed on free plan: `WeatherApiBulkRequestPactTest` (POST, 400, code 2009)

### Running contract tests
```bash
# Run all Pact tests via profile (includes **/*PactTest.java)
mvn -Pcontract test

# Or limit to contract tests by package/pattern
mvn -Dtest='com.example.contract.*PactTest' test

# Using Maven Wrapper (optional)
./mvnw -Pcontract test
./mvnw -Dtest='com.example.contract.*PactTest' test
```

Share the generated pact with the provider service (or a Pact Broker) for verification.

## Reports

- Cucumber HTML: `target/cucumber-report.html`
- Cucumber JSON: `target/cucumber.json`
- Allure results: `target/allure-results/`
  - View locally with Allure CLI:
    ```bash
    allure serve target/allure-results
    ```
- PrettyReports are also generated via plugin configuration

![Allure1](images/allure1.png)

![Allure2](images/allure2.png)

![Cucumber](images/cucumber.png)

## Notes

- Do not commit secrets. Provide the Weather API key via environment variables.
- Browser drivers are managed by your local browser/driver setup. Safari is macOS-only; Edge requires Windows.
- If you see CI-related flags in logs, they are set to make Chrome stable in containerized runners.
