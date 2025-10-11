# QA Automation Framework: UI • API • Security • Accessibility • Performance • Contract Testing

A unified test automation framework built with Java 21, Maven, Cucumber, Selenium, Rest Assured, axe-core/selenium, and Gatling. It supports BDD-style scenarios for UI and API testing, security header checks, accessibility scanning, and performance load tests.

- Detailed framework guide: see [readmeINFO.md](readmeINFO.md)
- Performance quick guide: see [HOW_TO_RUN_PERFORMANCE_TESTS.md](docs/HOW_TO_RUN_PERFORMANCE_TESTS.md)

## Prerequisites

- Java 21 (JDK). Ensure JAVA_HOME points to your JDK 21 installation
- Maven 3.9+
- IntelliJ IDEA (recommended)
- Optional: Allure CLI for viewing local Allure reports

## Quick Start

```bash
# 1) Clone and enter the project
git clone <your-repo-url>
cd qa-assessment-Eyup-Tozcu

# 2) Create your local config from the template
cp configuration.properties.template configuration.properties

# 3) Run tests (default runner excludes @accessibility and @security)
mvn clean test
```

## Configuration

- Browser selection via `configuration.properties` or system property:
  - Supported: `chrome`, `chrome-headless`, `firefox`, `firefox-headless`, `safari` (macOS), `edge` (Windows)
  - Example: `mvn clean test -Dbrowser=chrome-headless`
  - Headless toggle: `-Dheadless=true` (defaults to headless on CI, headed locally)
- SauceDemo URL and sample creds are defined in the config template
- Weather API base URL is configured in the template; provide your key via env var:
  ```bash
  export WEATHER_API_KEY=your_api_key
  ```

## Running Tests

- Default test run (UI/API; excludes accessibility and security via runner tags)
  ```bash
  mvn clean test
  ```
- Filter by tags (Cucumber v6)
  ```bash
  mvn clean test -Dcucumber.filter.tags="@smoke"
  mvn clean test -Dcucumber.filter.tags="not @accessibility and not @security"
  ```
- Run a specific feature
  ```bash
  mvn clean test -Dcucumber.features=src/test/resources/features/ui/ui_login.feature
  ```
- Choose browser / headless
  ```bash
  mvn clean test -Dbrowser=firefox -Dheadless=true
  ```

### Runners

- UI/API default: `com.example.runners.CukesRunner`
- Accessibility only: `com.example.runners.AccessibilityRunner` (tags: `@accessibility`)
- Security suite: `com.example.runners.SecurityRunner` (tags: `@security`)
- Performance (JUnit suite): `com.example.runners.PerformanceRunner`
- Performance (Cucumber): `com.example.runners.PerformanceCukesRunner` (default tags: `@performance`)

You can run these directly from your IDE or leave discovery to Maven Surefire (already configured in `pom.xml`).

## Security Test Suite

This project includes a Cucumber-based security suite for both the public website (SauceDemo) and the Weather API.

- Feature files
  - Web: `src/test/resources/features/security/saucedemo_security.feature`
  - API: `src/test/resources/features/security/weather_api_security.feature`
- Tags
  - `@security`: All security scenarios
  - `@web`: Web (SauceDemo) security scenarios
  - `@api`: Weather API security scenarios
  - `@requires_key`: Scenarios requiring a valid Weather API key
  - `@rate_limit`: Rate-limiting scenarios (may produce 429s; excluded by default in CI)
  - `@no_hsts`: Use relaxed API baseline (do not require HSTS header for JSON endpoints)
- Baselines
  - HTML baseline (web pages): X-Content-Type-Options, X-Frame-Options or CSP frame-ancestors, Referrer-Policy, Permissions-Policy, HSTS
  - API baseline (JSON): X-Content-Type-Options (+ HSTS unless `@no_hsts`)

### Setup

```bash
cp configuration.properties.template configuration.properties
export WEATHER_API_KEY=your_api_key_here
```

### Run recipes

```bash
# All security tests
mvn clean test -Dcucumber.filter.tags='@security'

# Web-only (SauceDemo)
mvn clean test -Dcucumber.filter.tags='@security and @web'

# API-only (requires WEATHER_API_KEY)
mvn clean test -Dcucumber.filter.tags='@security and @api'

# Exclude rate-limit scenarios
mvn clean test -Dcucumber.filter.tags='@security and not @rate_limit'

# Use relaxed API baseline (no HSTS)
mvn clean test -Dcucumber.filter.tags='@security and @api and @no_hsts'

# Configure rate-limit call count (override feature default)
mvn clean test -Dcucumber.filter.tags='@security and @api and @rate_limit' -DrateLimit.calls=10
```

### OAuth Security Tests

We provide realistic OAuth client-credentials tests with two providers. An OAuth toggle helper (`OAuthConfig`) prefers environment variables and falls back to `configuration.properties`.

- Precedence for oauth.* keys:
  1. Environment variables: `OAUTH_TOKEN_URL`, `OAUTH_CLIENT_ID`, `OAUTH_CLIENT_SECRET`, `OAUTH_SCOPE`, `OAUTH_PROBE_URL`
  2. System properties: `-Doauth.token_url=...` etc.
  3. `configuration.properties`: `oauth.token_url=...` etc.

- Default provider (Duende demo): already configured in `configuration.properties`
  ```bash
  # Run all @oauth scenarios
  mvn clean test -Dtest=SecurityRunner -Dcucumber.filter.tags='@oauth'
  ```

- Spotify (opaque tokens): set env vars, then run only @spotify
  ```bash
  export OAUTH_TOKEN_URL="https://accounts.spotify.com/api/token"
  export OAUTH_CLIENT_ID="YOUR_SPOTIFY_CLIENT_ID"
  export OAUTH_CLIENT_SECRET="YOUR_SPOTIFY_CLIENT_SECRET"
  export OAUTH_SCOPE=""
  export OAUTH_PROBE_URL="https://api.spotify.com/v1/search?q=daft%20punk&type=artist&limit=1"

  mvn clean test -Dtest=SecurityRunner -Dcucumber.filter.tags='@spotify'
  ```

- More details and cURL smoke tests: see [docs/OAUTH_DEMO_PROVIDERS.md](docs/OAUTH_DEMO_PROVIDERS.md)

## Accessibility

Mark features or scenarios with `@accessibility` and run via the runner or tag filter:

```bash
mvn clean test -Dcucumber.filter.tags='@accessibility'
```

## Reports

- Cucumber HTML: `target/cucumber-report.html`
- Cucumber JSON: `target/cucumber.json`
- Allure results: `target/allure-results/`
  - Produced when running Cucumber runners (e.g., `AllCukesRunner`, `ConractCukesRunner`). Pact-only JUnit runs via `-Pcontract` do not emit Allure results unless you add the Allure JUnit adapter.
  - Generate Allure HTML report with Maven:
    ```bash
    mvn io.qameta.allure:allure-maven:report
    ```

View Allure locally:

```bash
allure serve target/allure-results
```

## Performance Testing (Gatling)

Start here for the complete guide: [HOW_TO_RUN_PERFORMANCE_TESTS.md](docs/HOW_TO_RUN_PERFORMANCE_TESTS.md)

Quick start:

```bash
export WEATHER_API_KEY=your_api_key
./quick-perf-test.sh

# or run directly with Maven
mvn gatling:test \
  -Dgatling.simulationClass=com.example.performance.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=5 -Dperf.duration=30
```

### Performance Testing (JUnit Runner)

You can also run REST Assured-based performance tests via a dedicated JUnit suite runner.

```bash
# Prerequisite
export WEATHER_API_KEY=your_api_key

# Run the JUnit performance suite
mvn -Dtest=PerformanceRunner test

# Optional overrides
mvn -Dtest=PerformanceRunner test \
  -Dperf.users=10 -Dperf.rampup=10 -Dperf.duration=60
```

### Performance Testing (Cucumber)

A Cucumber runner is available for quick perf checks using the `@performance` feature(s).

```bash
# Prerequisite for Weather examples
export WEATHER_API_KEY=your_api_key

# Default run: executes @performance
mvn -Dtest=PerformanceCukesRunner test

# Run all @performance scenarios with explicit filter (optional)
mvn -Dtest=PerformanceCukesRunner -Dcucumber.filter.tags='@performance' test

# Tune loads quickly in CI/local using -Dperf.*
mvn -Dtest=PerformanceCukesRunner test \
  -Dperf.threads=5 \
  -Dperf.requests=50 \
  -Dperf.maxUsers=20 \
  -Dperf.rampupSeconds=5 \
  -Dperf.requestsPerUser=3
```

The starter feature lives at `src/test/resources/features/performance/perf_smoke.feature`.

## Contract Testing (Pact)

This repository includes a Pact JVM consumer test suite for the Weather API under `src/test/java/com/example/contract`.

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

### BDD Contract Scenarios (Cucumber)
You can run the same Pact tests through Cucumber BDD for unified reporting and tagging.

- Feature: `src/test/resources/features/contract/weather_api_contracts.feature`
- Runner: `com.example.runners.ConractCukesRunner` (targets `@contract` tag)

Run examples:
```bash
mvn -Dtest=ConractCukesRunner test
./mvnw -Dtest=ConractCukesRunner test
```

## Further Reading

- Detailed framework overview and usage: [readmeINFO.md](readmeINFO.md)
- Performance testing recipes and troubleshooting: [HOW_TO_RUN_PERFORMANCE_TESTS.md](docs/HOW_TO_RUN_PERFORMANCE_TESTS.md)

## Housekeeping

- Allure results are generated under `target/allure-results/` during local runs. If you see a root-level `allure-results/` directory, it likely contains stale, committed artifacts from earlier runs. These have been removed and the directory is ignored going forward.
