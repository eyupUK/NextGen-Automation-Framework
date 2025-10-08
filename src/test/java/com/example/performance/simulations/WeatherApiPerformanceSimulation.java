package com.example.performance.simulations;

import com.example.performance.config.PerformanceConfig;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Weather API Performance Simulation
 * <p>
 * This simulation tests the Weather API under various load conditions:
 * - Load Test: Gradual ramp-up to verify system behavior under expected load
 * - Stress Test: Push the system beyond normal capacity to find breaking points
 * - Spike Test: Sudden burst of traffic to test system resilience
 * <p>
 * Industry Best Practices Implemented:
 * 1. Realistic user behavior modeling
 * 2. Think time between requests
 * 3. Assertions on response times and success rates
 * 4. Comprehensive error handling
 * 5. Detailed reporting with percentiles
 */
public class WeatherApiPerformanceSimulation extends Simulation {

    // HTTP Protocol Configuration
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(PerformanceConfig.WEATHER_API_BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Performance-Test-Suite/1.0");

    // Scenario: Current Weather API
    private final ScenarioBuilder currentWeatherScenario = scenario("Current Weather API Load Test")
            .exec(
                    http("Get Current Weather - London")
                            .get("/current.json")
                            .queryParam("key", PerformanceConfig.WEATHER_API_KEY)
                            .queryParam("q", "London")
                            .check(status().is(200))
                            .check(jsonPath("$.location.name").exists())
                            .check(responseTimeInMillis().lte(2000)) // 2 second SLA
            )
            .pause(Duration.ofSeconds(1), Duration.ofSeconds(3)) // Think time
            .exec(
                    http("Get Current Weather - New York")
                            .get("/current.json")
                            .queryParam("key", PerformanceConfig.WEATHER_API_KEY)
                            .queryParam("q", "New York")
                            .check(status().is(200))
                            .check(jsonPath("$.location.name").exists())
            )
            .pause(Duration.ofSeconds(1), Duration.ofSeconds(2))
            .exec(
                    http("Get Current Weather - Tokyo")
                            .get("/current.json")
                            .queryParam("key", PerformanceConfig.WEATHER_API_KEY)
                            .queryParam("q", "Tokyo")
                            .check(status().is(200))
                            .check(jsonPath("$.location.name").exists())
            );

    // Scenario: Forecast API
    private final ScenarioBuilder forecastScenario = scenario("Forecast API Load Test")
            .exec(
                    http("Get 3-Day Forecast - Paris")
                            .get("/forecast.json")
                            .queryParam("key", PerformanceConfig.WEATHER_API_KEY)
                            .queryParam("q", "Paris")
                            .queryParam("days", "3")
                            .check(status().is(200))
                            .check(jsonPath("$.forecast.forecastday").exists())
                            .check(responseTimeInMillis().lte(3000)) // 3 second SLA for forecast
            )
            .pause(Duration.ofSeconds(2), Duration.ofSeconds(4));

    // Load Test Profile: Gradual ramp-up
    private final OpenInjectionStep loadTestProfile = rampUsers(PerformanceConfig.USERS)
            .during(Duration.ofSeconds(PerformanceConfig.RAMP_UP_TIME));

    // Stress Test Profile: Heavy load
    private final OpenInjectionStep stressTestProfile =
            constantUsersPerSec(5).during(Duration.ofSeconds(60));

    // Spike Test Profile: Sudden burst
    private final OpenInjectionStep spikeTestProfile =
            atOnceUsers(PerformanceConfig.USERS);

    {
        // Setup scenarios with injection profiles
        // Choose test type via system property: -Dperf.type=load|stress|spike
        String testType = System.getProperty("perf.type", "load");

        OpenInjectionStep injectionProfile;
        switch (testType.toLowerCase()) {
            case "stress":
                injectionProfile = stressTestProfile;
                break;
            case "spike":
                injectionProfile = spikeTestProfile;
                break;
            default:
                injectionProfile = loadTestProfile;
                break;
        }

        setUp(
                currentWeatherScenario.injectOpen(injectionProfile).protocols(httpProtocol),
                forecastScenario.injectOpen(
                        rampUsers(PerformanceConfig.USERS / 2)
                                .during(Duration.ofSeconds(PerformanceConfig.RAMP_UP_TIME))
                ).protocols(httpProtocol)
        )
                // Global assertions for SLA validation
                .assertions(
                        global().responseTime().percentile3().lte(PerformanceConfig.RESPONSE_TIME_P95_THRESHOLD),
                        global().responseTime().percentile4().lte(PerformanceConfig.RESPONSE_TIME_P99_THRESHOLD),
                        global().successfulRequests().percent().gte(PerformanceConfig.SUCCESS_RATE_THRESHOLD),
                        global().responseTime().mean().lte(1000) // Mean response time under 1 second
                );
    }
}

