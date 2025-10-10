package com.example.contract;

    import au.com.dius.pact.consumer.dsl.DslPart;
    import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
    import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
    import au.com.dius.pact.consumer.junit.PactProviderRule;
    import au.com.dius.pact.consumer.junit.PactVerification;
    import au.com.dius.pact.core.model.RequestResponsePact;
    import au.com.dius.pact.core.model.annotations.Pact;
    import io.restassured.RestAssured;
    import org.junit.Rule;
    import org.junit.Test;

    import java.util.Collections;

    import static io.restassured.RestAssured.given;
    import static org.hamcrest.Matchers.*;

    public class WeatherApiConsumerPactTest {

        @Rule
        public PactProviderRule mockProvider = new PactProviderRule("WeatherProvider", "localhost", 0, this);

        @Pact(consumer = "QAFrameworkConsumer")
        public RequestResponsePact londonWeatherPact(PactDslWithProvider builder) {
            DslPart body = new PactDslJsonBody()
                .object("location")
                    .stringValue("name", "London")
                .closeObject()
                .object("current")
                    .decimalType("temp_c", 12.3)
                .closeObject();

            return builder
                .given("weather exists for London")
                .uponReceiving("request for current weather in London")
                    .path("/v1/current.json")
                    .method("GET")
                    .query("q=London")
                .willRespondWith()
                    .status(200)
                    .headers(Collections.singletonMap("Content-Type", "application/json"))
                    .body(body)
                .toPact();
        }

        @Pact(consumer = "QAFrameworkConsumer")
        public RequestResponsePact londonWeatherWithAqiPact(PactDslWithProvider builder) {
            DslPart body = new PactDslJsonBody()
                .object("location")
                    .stringValue("name", "London")
                .closeObject()
                .object("current")
                    .decimalType("temp_c", 10.0)
                    .booleanType("is_day", true)
                    .object("air_quality")
                        .numberType("pm2_5", 5.4)
                        .numberType("pm10", 10.2)
                    .closeObject()
                .closeObject();

            return builder
                .given("weather with AQI exists for London")
                .uponReceiving("request for current weather in London with AQI")
                    .path("/v1/current.json")
                    .method("GET")
                    .query("q=London&aqi=yes")
                .willRespondWith()
                    .status(200)
                    .headers(Collections.singletonMap("Content-Type", "application/json"))
                    .body(body)
                .toPact();
        }

        @Pact(consumer = "QAFrameworkConsumer")
        public RequestResponsePact cityNotFoundPact(PactDslWithProvider builder) {
            DslPart body = new PactDslJsonBody()
                .object("error")
                    .integerType("code", 1006)
                    .stringType("message", "No matching location found.")
                .closeObject();

            return builder
                .given("no weather exists for UnknownCity")
                .uponReceiving("request for current weather in an unknown city")
                    .path("/v1/current.json")
                    .method("GET")
                    .query("q=UnknownCity")
                .willRespondWith()
                    .status(400)
                    .headers(Collections.singletonMap("Content-Type", "application/json"))
                    .body(body)
                .toPact();
        }

        @Pact(consumer = "QAFrameworkConsumer")
        public RequestResponsePact forecast3DaysPact(PactDslWithProvider builder) {
            DslPart body = new PactDslJsonBody()
                .object("location")
                    .stringValue("name", "London")
                .closeObject()
                .object("forecast")
                    .array("forecastday")
                        .object()
                            .object("day")
                                .decimalType("maxtemp_c", 15.0)
                                .decimalType("mintemp_c", 8.0)
                            .closeObject()
                        .closeObject()
                        .object()
                            .object("day")
                                .decimalType("maxtemp_c", 16.0)
                                .decimalType("mintemp_c", 7.0)
                            .closeObject()
                        .closeObject()
                        .object()
                            .object("day")
                                .decimalType("maxtemp_c", 14.0)
                                .decimalType("mintemp_c", 6.5)
                            .closeObject()
                        .closeObject()
                    .closeArray()
                .closeObject();

            return builder
                .given("3-day forecast exists for London")
                .uponReceiving("request for 3-day forecast in London")
                    .path("/v1/forecast.json")
                    .method("GET")
                    .query("q=London&days=3")
                .willRespondWith()
                    .status(200)
                    .headers(Collections.singletonMap("Content-Type", "application/json"))
                    .body(body)
                .toPact();
        }

        @Test
        @PactVerification(fragment = "londonWeatherPact")
        public void verifiesCurrentWeatherLondon() {
            RestAssured.baseURI = "http://localhost:" + mockProvider.getPort();

            given()
            .when()
                .get("/v1/current.json?q=London")
            .then()
                .statusCode(200)
                .body("location.name", equalTo("London"))
                .body("current.temp_c", notNullValue());
        }

        @Test
        @PactVerification(fragment = "londonWeatherWithAqiPact")
        public void verifiesCurrentWeatherLondonWithAqi() {
            RestAssured.baseURI = "http://localhost:" + mockProvider.getPort();

            given()
            .when()
                .get("/v1/current.json?q=London&aqi=yes")
            .then()
                .statusCode(200)
                .body("location.name", equalTo("London"))
                .body("current.air_quality.pm2_5", notNullValue())
                .body("current.air_quality.pm10", notNullValue());
        }

        @Test
        @PactVerification(fragment = "cityNotFoundPact")
        public void verifiesCityNotFound() {
            RestAssured.baseURI = "http://localhost:" + mockProvider.getPort();

            given()
            .when()
                .get("/v1/current.json?q=UnknownCity")
            .then()
                .statusCode(400)
                .body("error.code", equalTo(1006))
                .body("error.message", containsString("No matching location"));
        }

        @Test
        @PactVerification(fragment = "forecast3DaysPact")
        public void verifiesForecast3DaysLondon() {
            RestAssured.baseURI = "http://localhost:" + mockProvider.getPort();

            given()
            .when()
                .get("/v1/forecast.json?q=London&days=3")
            .then()
                .statusCode(200)
                .body("location.name", equalTo("London"))
                .body("forecast.forecastday.size()", equalTo(3))
                .body("forecast.forecastday[0].day.maxtemp_c", notNullValue())
                .body("forecast.forecastday[0].day.mintemp_c", notNullValue());
        }
    }