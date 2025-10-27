# Deterministic Demo PoC

This repo now includes a fully deterministic end-to-end demo for stakeholders that avoids flaky external dependencies.

What’s included:
- UI demo of the checkout flow on SauceDemo, driven by a tiny in-process mock server.
- API demo of Weather API scenarios (current, forecast, errors), backed by a local mock server that returns schema-compliant payloads.
- Security gate before tests: SpotBugs + FindSecBugs and OWASP Dependency-Check run on CI to block critical issues early.
- Stable reruns: a dedicated `rerun-failed` profile that prepares `target/rerun.txt` and runs only failed scenarios safely.

## Quick start

UI demo (headless Chrome, mocked SauceDemo pages):

```bash
./mvnw -Pdemo -Dheadless=true -Dbrowser=chrome -Ddemo.mock=true -Dcucumber.filter.tags="@demo" test
```

API demo (mocked Weather API):

```bash
./mvnw -q -Pdemo -Ddemo.mock.api=true -Dcucumber.filter.tags="@api and @current" test
```

You can mix and match tags, for example to run only error scenarios:

```bash
./mvnw -Pdemo -Ddemo.mock.api=true -Dcucumber.filter.tags="@api and @errors" test
```

## Rerun only failed scenarios

By default, the rerun runner is excluded so the build doesn’t fail when `target/rerun.txt` doesn’t exist.
To rerun only failed scenarios after an initial run:

```bash
# 1) Run your suite (examples)
./mvnw test

# 2) Now run only the failed scenarios
./mvnw -Prerun-failed test
```

The profile will `touch target/rerun.txt` to avoid parser errors and execute `FailedTestRunner` only.

## Security Gate (CI)

A security gate runs early in CI to fail fast on critical risks.

- Static analysis: SpotBugs with FindSecBugs (High priority, include tests)
- Dependency scanning: OWASP Dependency-Check (fail on CVSS v3 >= 9.0)
- Reports are uploaded as the `security-reports` artifact.

Local runs (optional):
```bash
./mvnw -Psecurity-gate -DskipTests=true verify
```

## Notes

- Chrome CDP warnings for new browser versions are harmless; if desired, add matching `selenium-devtools-v<ver>` dependency.
- Secrets (e.g., `WEATHER_API_KEY`) aren’t required when using the mock; for real provider tests, set an env var or `-DWEATHER_API_KEY`. Set `-Dweather_api_base_url` to point to the real API.
- Demo artifacts (screenshots, Allure results) are written under `target/` and can be uploaded in CI.
