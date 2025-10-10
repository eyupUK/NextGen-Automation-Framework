# Contract Testing with Pact (Consumer & Provider)

This project is Pact-ready. Follow this guide to enable consumer-driven contract tests using Pact JVM.

## Why Pact?
- Catch breaking API changes early by testing consumer expectations against provider responses.
- Share versioned contracts via a Pact Broker (optional) to automate verification in CI/CD.

## Prerequisites
- Java 21, Maven 3.9+
- Internet access to download Pact dependencies (or a pre-populated local Maven cache)

## Option A — Quick start (Consumer, JUnit 4)

1) Add dependencies to `pom.xml` (test scope):

```xml
<!-- Pact JVM: Consumer JUnit4 -->
<dependency>
  <groupId>au.com.dius.pact.consumer</groupId>
  <artifactId>junit</artifactId>
  <version>4.6.15</version>
  <scope>test</scope>
</dependency>
```

Notes:
- If your environment can’t resolve the dependency (offline/air‑gapped), add Pact to a profile and enable it only in CI or a connected dev machine.
- For JUnit 5, use `au.com.dius.pact.consumer:junit5` instead.

2) Create a consumer test (example):

```java
package com.example.contract;

import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class WeatherApiConsumerPactTest {

  @Rule
  public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("WeatherProvider", "localhost", 0, this);

  @Pact(consumer = "QAFrameworkConsumer")
  public RequestResponsePact createPact(PactDslWithProvider builder) {
    PactDslJsonBody responseBody = new PactDslJsonBody()
      .object("location").stringValue("name", "London").closeObject()
      .object("current").decimalType("temp_c", 12.3).closeObject();

    return builder
      .given("weather exists for London")
      .uponReceiving("a request for current weather in London")
        .path("/v1/current.json")
        .method("GET")
        .query("q=London")
      .willRespondWith()
        .status(200)
        .headers("Content-Type", "application/json")
        .body(responseBody)
      .toPact();
  }

  @Test
  @PactVerification("WeatherProvider")
  public void verifiesContract() {
    RestAssured.baseURI = "http://localhost:" + mockProvider.getPort();

    given()
      .when().get("/v1/current.json?q=London")
      .then()
        .statusCode(200)
        .body("location.name", equalTo("London"))
        .body("current.temp_c", notNullValue());
  }
}
```

3) Run the consumer test

```bash
mvn -Dtest=WeatherApiConsumerPactTest test
```

4) Find the generated pact file
- `target/pacts/QAFrameworkConsumer-WeatherProvider.json`

## Option B — Provider verification (JUnit 4)

Add provider dependency:

```xml
<dependency>
  <groupId>au.com.dius.pact.provider</groupId>
  <artifactId>junit</artifactId>
  <version>4.6.15</version>
  <scope>test</scope>
</dependency>
```

Example skeleton:

```java
@RunWith(au.com.dius.pact.provider.junit.PactRunner.class)
@Provider("WeatherProvider")
@PactFolder("target/pacts") // Or @PactBroker
public class WeatherProviderPactTest {

  @TestTarget
  public final Target target = new HttpTarget("http", "localhost", 8080, "/");

  @State("weather exists for London")
  public void setupState() {
    // Start provider, seed data, or stub external dependencies
  }
}
```

Run provider verification after starting your real service on port 8080:

```bash
mvn -Dtest=WeatherProviderPactTest test
```

## Optional — Activate Pact via a Maven profile

If you want to keep Pact deps off by default, add a profile in `pom.xml`:

```xml
<profile>
  <id>pact</id>
  <dependencies>
    <dependency>
      <groupId>au.com.dius.pact.consumer</groupId>
      <artifactId>junit</artifactId>
      <version>4.6.15</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>au.com.dius.pact.provider</groupId>
      <artifactId>junit</artifactId>
      <version>4.6.15</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
</profile>
```

Then run:

```bash
mvn -Ppact -Dtest=WeatherApiConsumerPactTest test
```

## Pact Broker (optional)
- Use a broker to publish and verify pacts across repos.
- CLI example (once you configure broker URL and credentials):

```bash
# Publish generated pacts
pact-broker publish target/pacts --consumer-app-version $(git rev-parse --short HEAD) \
  --broker-base-url https://your-broker --broker-token $PACT_BROKER_TOKEN
```

## Notes
- This repo includes a placeholder test `WeatherApiConsumerPactTest` that is skipped by default to keep builds light.
- To fully enable Pact, add the dependencies above and replace the placeholder with the real consumer test.
- If your environment cannot fetch dependencies, enable Pact only in CI or a dev machine that has internet access.

