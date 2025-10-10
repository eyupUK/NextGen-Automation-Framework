# Contract Testing with Pact (Consumer & Provider)

This project includes Pact JVM consumer testing out of the box. Use this guide to run the tests, generate pact files, and (optionally) verify them against a provider.

## What you get here
- A ready-to-run consumer test: `src/test/java/com/example/contract/WeatherApiConsumerPactTest.java`
- Four interactions based on common Weather API patterns:
  - Current weather (London)
  - Current weather with AQI (London)
  - City not found error (UnknownCity)
  - 3-day forecast (London)
- Pact files generated under `target/pacts/`

## Prerequisites
- Java 21, Maven 3.9+
- Internet access for Maven dependencies (or a pre-populated local cache)

## Dependencies (already included)
This repo already declares the Pact consumer dependency:

```xml
<dependency>
  <groupId>au.com.dius.pact.consumer</groupId>
  <artifactId>junit</artifactId>
  <version>4.3.6</version>
  <scope>test</scope>
</dependency>
```

If you wish to upgrade Pact later, bump the version in `pom.xml` after reviewing the Pact JVM release notes.

## Running consumer Pact tests
Note: run these commands from the project root (the folder that contains `pom.xml`), not from `docs/`.
Lines starting with `#` are comments; don’t paste them into your terminal.

```bash
# Run all Pact tests
mvn -Pcontract test

# Or, using the Maven Wrapper
./mvnw -Pcontract test

# Run only the Weather API consumer pact test
mvn -Pcontract -Dtest=WeatherApiConsumerPactTest test

# Or with the Maven Wrapper
./mvnw -Pcontract -Dtest=WeatherApiConsumerPactTest test
```

After a successful run, pact files are written to:
- `target/pacts/QAFrameworkConsumer-WeatherProvider.json`

## What the consumer expects (contracts)
The included test defines these interactions with the mock provider:
- GET `/v1/current.json?q=London` → 200 with basic current weather fields
- GET `/v1/current.json?q=London&aqi=yes` → 200 including `current.air_quality.*`
- GET `/v1/current.json?q=UnknownCity` → 400 with `{ error: { code, message } }`
- GET `/v1/forecast.json?q=London&days=3` → 200 with `forecast.forecastday[3]` entries

You can find and modify the test here:
- `src/test/java/com/example/contract/WeatherApiConsumerPactTest.java`

## Provider verification (optional)
If you have a real provider service, you can verify it against the generated pacts. Add the provider dependency if needed:

```xml
<dependency>
  <groupId>au.com.dius.pact.provider</groupId>
  <artifactId>junit</artifactId>
  <version>4.3.6</version>
  <scope>test</scope>
</dependency>
```

Example skeleton (JUnit 4):

```java
@RunWith(au.com.dius.pact.provider.junit.PactRunner.class)
@Provider("WeatherProvider")
@PactFolder("target/pacts") // Or use @PactBroker
public class WeatherProviderPactTest {

  @TestTarget
  public final Target target = new HttpTarget("http", "localhost", 8080, "/");

  @State("weather exists for London")
  public void stateWeatherExistsForLondon() { /* seed or stub */ }

  @State("weather with AQI exists for London")
  public void stateWeatherWithAqiExistsForLondon() { /* seed or stub */ }

  @State("no weather exists for UnknownCity")
  public void stateNoWeatherExistsForUnknownCity() { /* seed or stub */ }

  @State("3-day forecast exists for London")
  public void stateForecastExistsForLondon() { /* seed or stub */ }
}
```

Run provider verification after starting your service on port 8080:

```bash
mvn -Dtest=WeatherProviderPactTest test
```

## Pact Broker (optional)
If you use a Pact Broker, publish pacts from `target/pacts` and verify them in your provider pipeline.
A typical CLI invocation (configure broker URL/token first):

```bash
pact-broker publish target/pacts \
  --consumer-app-version "$(git rev-parse --short HEAD)" \
  --broker-base-url https://your-broker \
  --broker-token "$PACT_BROKER_TOKEN"
```

## Troubleshooting
- If tests don’t run, ensure you used the `-Pcontract` profile and that you’re in the project root.
- If dependencies fail to resolve, check your Maven settings or run once on a connected machine to warm the cache.
- Pact files not appearing? Verify the test completed successfully and that `target/` is not being cleaned by your IDE between runs.
