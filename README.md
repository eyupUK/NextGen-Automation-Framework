# NextGen Automation Framework: API • Contract • UI • Security • Accessibility • Performance Testing

[![Security Gate](https://img.shields.io/badge/security-gate-green)](#security-gate-sast--dependencies)
[![Sonar Quality Gate](https://img.shields.io/badge/Sonar-Quality%20Gate-blue)](#sonar-quality-gate)
[![Snyk](https://img.shields.io/badge/Snyk-OSS%20Scan-purple)](#snyk-open-source-scan)

A unified test automation framework built with Java 21, Maven, JUnit, Cucumber, Selenium, Rest Assured, axe-core/selenium, and Gatling. It supports BDD-style scenarios for UI and API testing, security header checks, accessibility scanning, and performance load tests.

- Detailed framework guide: see [readmeINFO.md](readmeINFO.md)
- Performance quick guide: see [HOW_TO_RUN_PERFORMANCE_TESTS.md](docs/HOW_TO_RUN_PERFORMANCE_TESTS.md)

## Prerequisites

- Java 21 (JDK). Ensure JAVA_HOME points to your JDK 21 installation
- Maven 3.9+ (recommended)
- IntelliJ IDEA (recommended)
- Optional: Allure CLI for viewing local Allure reports

## Quick Start

```bash
# 1) Clone and enter the project
git clone <your-repo-url>
cd <your-repo-directory>

# 2) Create your local config from the template
cp configuration.properties.template configuration.properties

# 3) Run tests (default runner)
mvn clean test
```

## Configuration

- Browser selection via `configuration.properties` or system property:
  - Supported: `chrome`, `chrome-headless`, `firefox`, `firefox-headless`, `safari` (macOS), `edge` (Windows)
  - Example: `mvn clean test -Dbrowser=chrome-headless`
  - Headless toggle: `-Dheadless=true` (defaults to headless on CI, headed locally)
- SauceDemo URL and sample creds are defined in the config template
- Weather API base URL is configurable; provide your key via env var when hitting the real API:
  ```bash
  export WEATHER_API_KEY=your_api_key
  ```
- Environment selection (for env-specific property files):
  - The loader looks for, in order: `configuration-<env>.properties` (if present), else `configuration.properties`.
  - Pick environment via system property or environment variable:
    ```bash
    # system property (preferred)
    mvn clean test -DENV=staging

    # alias
    mvn clean test -Denv=dev

    # environment variable
    export ENV=staging
    mvn clean test

    # alternate var if ENV conflicts
    export TEST_ENV=staging
    mvn clean test
    ```

## Running Tests

- Default test run (UI/API)
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

- Unified cucumber runner: `com.example.runners.AllCukesRunner` (used by Maven Surefire by default)
- UI/API (E2E) default: `com.example.runners.CukesRunner`
- Accessibility only: `com.example.runners.AccessibilityCukesRunner` (tags: `@accessibility`)
- Security suite: `com.example.runners.SecurityCukesRunner` (tags: `@security`)
- Performance (JUnit suite): `com.example.runners.PerformanceJUnitRunner`
- Performance (Cucumber): `com.example.runners.PerformanceCukesRunner` (default tags: `@performance`)

You can run these directly from your IDE or leave discovery to Maven Surefire. By default, Surefire includes only `**/*AllCukesRunner.java` and excludes `FailedTestRunner`. To rerun failed scenarios use the profile below or run the rerun runner explicitly.

### Deterministic demo runs

A fully deterministic demo is available for stakeholders using in-process mock servers.

- Quick start and details: see [docs/DEMO_README.md](docs/DEMO_README.md)
- UI demo (@demo tag, mock UI server):
  ```bash
  ./mvnw -Pdemo -Dheadless=true -Dbrowser=chrome -Ddemo.mock=true -Dcucumber.filter.tags="@demo" test
  ```
- API demo (mock Weather API):
  ```bash
  ./mvnw -Pdemo -Ddemo.mock.api=true -Dcucumber.filter.tags="@api and @current" test
  ```

### Security Gate (SAST + Dependencies)

Run security checks before tests to keep the codebase clean and secure from the start.

- SAST: SpotBugs + FindSecBugs (High priority)
- Dependency: OWASP Dependency-Check (fail on CVSS v3 ≥ 8.0)
- License/SBOM: THIRD-PARTY inventory and CycloneDX BOM (target/bom.json)

Local usage:

```bash
./mvnw -Psecurity-gate -DskipTests=true verify
# With fresh NVD DB
./mvnw -Psecurity-gate -Ddependency-check.autoUpdate=true -DskipTests=true verify
```

- Suppressions: edit `config/dependency-check-suppressions.xml` to whitelist known false positives (with rationale and expiry).
- CI: see `.github/workflows/ci.yml` (first job).

### Sonar Quality Gate

CI runs a Sonar analysis and waits for the Quality Gate. Configure your secrets:
- `SONAR_HOST_URL` (e.g., `https://sonarcloud.io`)
- `SONAR_TOKEN`
- Optional: `SONAR_PROJECT_KEY`, `SONAR_ORG` (override the defaults in `sonar-project.properties`).

Local analysis (when using a local SonarQube):
```bash
./mvnw -DskipTests=true org.sonarsource.scanner.maven:sonar-maven-plugin:3.10.0.2594:sonar \
  -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_TOKEN
```

### Snyk Open Source Scan

CI runs `snyk test` to fail on policy violations, and `snyk monitor` to keep an inventory snapshot in Snyk.

- Set repo secret: `SNYK_TOKEN`
- Artifacts: SARIF uploaded to code scanning alerts in the repository (Security tab)

Manual run (requires Snyk CLI):
```bash
snyk auth $SNYK_TOKEN
snyk test --maven --severity-threshold=high --fail-on=all
snyk monitor --maven --project-name="$(basename "$PWD")"
```

...existing code...

## CI (GitHub Actions)

This repo includes production-ready workflows under `.github/workflows/`:

- Security Gate: Embedded in `ci.yml` as the first job
- Cucumber test suites
  - `cucumber-ui-api.yml` (UI + API)
  - `cucumber-accessibility.yml`
  - `cucumber-security.yml`
  - `cucumber-contract.yml`
  - `cucumber-performance.yml`
  - Triggers: push and pull_request on main/master
  - Reliability: failed scenarios are rerun via `FailedTestRunner` when a rerun list exists
  - Artifacts: Cucumber HTML/JSON, Allure results, rerun files, and any failed HTML pages/screenshots
  - Summary: each job writes a short “Cucumber Summary” to `$GITHUB_STEP_SUMMARY`

- Performance and PR checks
  - `performance-tests.yml`, `nightly-performance.yml`, `pr-performance.yml`, `stress-test.yml`

Notes
- Workflows run with least-privilege permissions and concurrency to prevent overlapping runs.

### Quick Links to GitHub Actions

Replace `OWNER/REPO` with your GitHub org/user and repository:

- All workflow runs: `https://github.com/OWNER/REPO/actions`
- UI + API (E2E): `https://github.com/OWNER/REPO/actions/workflows/cucumber-ui-api.yml`
- Accessibility: `https://github.com/OWNER/REPO/actions/workflows/cucumber-accessibility.yml`
- Security: `https://github.com/OWNER/REPO/actions/workflows/cucumber-security.yml`
- Contract: `https://github.com/OWNER/REPO/actions/workflows/cucumber-contract.yml`
- Cucumber Performance: `https://github.com/OWNER/REPO/actions/workflows/cucumber-performance.yml`
- CI (tagged API/UI/Accessibility): `https://github.com/OWNER/REPO/actions/workflows/ci.yml`
- Performance Tests: `https://github.com/OWNER/REPO/actions/workflows/performance-tests.yml`
- Nightly Performance: `https://github.com/OWNER/REPO/actions/workflows/nightly-performance.yml`
- PR Performance: `https://github.com/OWNER/REPO/actions/workflows/pr-performance.yml`
- Stress Test: `https://github.com/OWNER/REPO/actions/workflows/stress-test.yml`

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
  - Produced when running Cucumber runners (e.g., `AllCukesRunner`, `ContractCukesRunner`). Pact-only JUnit runs via `-Pcontract` do not emit Allure results unless you add the Allure JUnit adapter.
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
  -Dgatling.simulationClass=com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation \
  -Dperf.users=5 -Dperf.duration=30
```

- Convenience script: `./run-performance-tests.sh` now defaults to `weather-api` when no command is provided and automatically opens the latest Gatling HTML report after `weather-api`, `ecommerce-api`, and `all-gatling` runs.
- To open the last report anytime: `./run-performance-tests.sh report`

### Programmatic Gatling Runner (JUnit/Main)

You can run Gatling simulations without the Maven plugin using a lightweight runner.

- JUnit (via Surefire):
  ```bash
  mvn -Dtest=GatlingTestsRunner test
  ```
- Select a specific simulation:
  ```bash
  mvn -Dgatling.simulationClass=com.example.performance.gatling.simulations.EcommerceApiPerformanceSimulation \
      -Dtest=GatlingTestsRunner test
  ```
- From IDE/CLI main method:
  - Run `com.example.runners.GatlingTestsRunner` (optionally pass the simulation class as the first arg or via `-Dgatling.simulationClass=...`).

Notes
- The Gatling Maven Plugin is configured with `<includes>` to allow multiple simulations. You can still target a single simulation with `-Dgatling.simulationClass=...` on the CLI.

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
- Runner: `com.example.runners.ContractCukesRunner` (targets `@contract` tag)

Run examples:
```bash
mvn -Dtest=ContractCukesRunner test
./mvnw -Dtest=ContractCukesRunner test
```

## Further Reading

- Detailed framework overview and usage: [readmeINFO.md](readmeINFO.md)
- Performance testing recipes and troubleshooting: [HOW_TO_RUN_PERFORMANCE_TESTS.md](docs/HOW_TO_RUN_PERFORMANCE_TESTS.md)

## Housekeeping

- Allure results are generated under `target/allure-results/` during local runs. If you see a root-level `allure-results/` directory, it likely contains stale, committed artifacts from earlier runs. These have been removed and the directory is ignored going forward.

### Maven CLI notes (multi-line usage and ENV)
If you see `Unknown lifecycle phase` when using multi-line commands, it's almost always due to a bad line continuation. Either use a single line or ensure each line ends with a backslash with no trailing spaces. Also remember: pass environment selections with `-DENV=...`, not as a bare token.

```bash
# Single line (recommended)
mvn -B -V -Dheadless=true -DWEATHER_API_KEY="$WEATHER_API_KEY" -DENV=staging -Dtest=CukesRunner test

# Multi-line (correct)
mvn -B -V -Dheadless=true \
  -DWEATHER_API_KEY="$WEATHER_API_KEY" \
  -DENV=staging \
  -Dtest=CukesRunner test
```
