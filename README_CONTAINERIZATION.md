1. Build and run with docker-compose (recommended):

```bash
# Build selenium image and the test container, then run tests. Exit code will come from the tests.
docker-compose up --build --exit-code-from ui-tests
```

2. Or run selenium and then the test container separately:

```bash
# Start standalone selenium (keeps running)
docker-compose up -d selenium

# Build and run the test container (it will run tests and exit)
docker build -t ui-tests-runner .

docker run --rm --network host -e SELENIUM_URL=http://host.docker.internal:4444 ui-tests-runner
```

Notes about networking
- `docker-compose` places both services on the same user-defined network so the tests can use the simple URL `http://selenium:4444`.
- When running containers separately or on macOS, use `host.docker.internal` to reach services on the host.

Driver configuration
- Remote Selenium URL: provide `-Dwebdriver.remote.url=http://selenium:4444` (system property) or set `SELENIUM_URL` environment variable.
- Browser selection: `-Dbrowser=chrome|chrome-headless|firefox|...` (falls back to configuration.properties value or `chrome`).
- Headless selection: `-Dheadless=true|false` or environment `CI=true` will default to headless in CI.

Docker-compose notes
- The compose file uses `selenium/standalone-chrome:117.0` and increases `shm_size` to 2GB to reduce Chrome crashes.
- Adjust `SE_NODE_MAX_SESSIONS` or Chrome tag version if you need a particular release.

Troubleshooting
- If Chrome crashes with SIGSEGV in CI, add/keep `--no-sandbox` and `--disable-dev-shm-usage` (already added in `Driver.java`).
- If tests can't connect to Selenium, verify `SELENIUM_URL` and container network; check `docker-compose logs selenium` for errors.

Requirements coverage
- Add Dockerfile and compose to run UI tests: Done
- Make tests use remote Selenium when available: Done (Driver reads `webdriver.remote.url` and `SELENIUM_URL`)
- Provide run instructions: Done (this README)

Next steps / optional
- Add a GitHub Actions workflow to run `docker-compose up --build --exit-code-from ui-tests` on PRs.
- Parallelize test shards by splitting the suite into multiple containers.

Containerizing UI tests

What I added
- `Dockerfile` — multi-stage image: builds the project then runs `mvn test` in a runtime image.
- `docker-compose.yml` — starts a `selenium/standalone-chrome` service and the `ui-tests` container that runs your tests against the Selenium service.
- `Driver.java` (test code) — added support for running tests against a remote Selenium URL provided via system property `-Dwebdriver.remote.url` or env var `SELENIUM_URL` (falls back to local ChromeDriver if not provided).

How to run locally


