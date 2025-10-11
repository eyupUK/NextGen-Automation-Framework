# Contract Testing with Pact (Consumer)

This project includes Pact JVM consumer testing out of the box. Use this guide to run the tests, generate pact files, and (optionally) verify them against a provider.

## What’s included
- Consumer tests under: `src/test/java/com/example/contract`
- Provider name: `WeatherAPI`
- Pact files generated to: `target/pacts/`

### Test classes and coverage
- `WeatherApiConsumerPactTest` — current weather by city (London)
- `WeatherApiCoordinatesPactTest` — current weather by coordinates (48.8567,2.3508 → Paris)
- `WeatherApiZipCodePactTest` — current weather by ZIP/postal (90201 → Bell Gardens)
- `WeatherApiForecastPactTest` — 3-day forecast (London)
- `WeatherApiErrorsPactTest` — missing query parameter error (400, code 1003)
- `WeatherApiInvalidLocationPactTest` — invalid/unknown location error (400, code 1006)
- `WeatherApiBulkRequestPactTest` — bulk request not allowed on free plan (POST, 400, code 2009)

Each test uses a Pact mock provider and builds request/response interactions using the Pact DSL.

## Prerequisites
- Java 21, Maven 3.9+
- Internet access for Maven dependencies (or a pre-populated local cache)

## Running consumer Pact tests
Run from the project root (folder containing `pom.xml`).

```bash
# Run all Pact tests via the dedicated profile (includes **/*PactTest.java)
mvn -Pcontract test

# Or restrict to the contract package/pattern
mvn -Dtest='com.example.contract.*PactTest' test

# Using the Maven Wrapper (optional)
./mvnw -Pcontract test
./mvnw -Dtest='com.example.contract.*PactTest' test
```

After a successful run, pact files are written to:
- `target/pacts/QAFrameworkConsumer-WeatherAPI.json`

## Mock server usage
Tests rely on the Pact mock server started by `PactProviderRule`. The base URL is obtained via `mockProvider.getUrl()`. This avoids hardcoded base URIs and allows tests to run reliably in parallel or on shared CI agents.

## What the consumer expects (examples)
The included tests define interactions like:
- GET `/v1/current.json?q=London` → 200 with basic current weather fields
- GET `/v1/current.json?q=48.8567,2.3508` → 200 with Paris location fields
- GET `/v1/current.json?q=90201` → 200 with Bell Gardens location fields
- GET `/v1/forecast.json?q=London&days=3` → 200 with 3 entries under `forecast.forecastday`
- GET `/v1/current.json` (missing `q`) → 400 `{ error: { code: 1003, message } }`
- GET `/v1/current.json?q=UnknownCity12345` → 400 `{ error: { code: 1006, message } }`
- POST `/v1/current.json` with bulk payload → 400 `{ error: { code: 2009, message } }`

## Provider verification (optional)
If you have a real provider service, verify it against the generated pacts. Example (JUnit 4):

```java
@RunWith(au.com.dius.pact.provider.junit.PactRunner.class)
@Provider("WeatherAPI")
@PactFolder("target/pacts") // Or use @PactBroker
public class WeatherProviderPactTest {

  @TestTarget
  public final Target target = new HttpTarget("http", "localhost", 8080, "/");

  @State("weather data exists for London")
  public void stateWeatherExistsForLondon() { /* seed or stub */ }

  @State("weather data exists for coordinates")
  public void stateWeatherExistsForCoordinates() { /* seed or stub */ }

  @State("weather data exists for zip code")
  public void stateWeatherExistsForZip() { /* seed or stub */ }

  @State("forecast data exists for London")
  public void stateForecastExistsForLondon() { /* seed or stub */ }

  @State("no query parameter provided")
  public void stateNoQueryParam() { /* seed or stub */ }

  @State("invalid location provided")
  public void stateInvalidLocation() { /* seed or stub */ }

  @State("bulk request on free plan")
  public void stateBulkRequestFreePlan() { /* seed or stub */ }
}
```

Run provider verification after starting your service on port 8080:

```bash
mvn -Dtest=WeatherProviderPactTest test
```

## Pact Broker (optional)
Publish pacts from `target/pacts` and verify them in your provider pipeline.

```bash
pact-broker publish target/pacts \
  --consumer-app-version "$(git rev-parse --short HEAD)" \
  --broker-base-url https://your-broker \
  --broker-token "$PACT_BROKER_TOKEN"
```

## Troubleshooting
- If tests don’t run, ensure you used the `-Pcontract` profile or a `-Dtest` filter matching `*PactTest` and that you’re in the project root.
- If dependencies fail to resolve, check your Maven settings or run once on a connected machine to warm the cache.
- Pact files not appearing? Verify the test completed successfully and that `target/` is not being cleaned by your IDE between runs.
