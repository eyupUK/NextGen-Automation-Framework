package com.example.performance.junit.tests;

import com.example.config.TestConfig;
import com.example.performance.config.PerformanceConfig;
import com.example.performance.utils.PerformanceMetricsCollector;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

/**
 * REST Assured based Performance Tests
 * Tests various endpoints of a Weather API under different load conditions
 * 1. Concurrent load generation using ExecutorService
 * 2. Comprehensive metrics collection
 * 3. SLA validation
 * 4. Proper resource management
 * 5. Detailed reporting
 */
public class WeatherApiPerformanceTest {

    private PerformanceMetricsCollector metricsCollector;
    private ExecutorService executorService;

    @Before
    public void setUp() {
        metricsCollector = new PerformanceMetricsCollector("Weather API Performance Test");
        executorService = Executors.newFixedThreadPool(PerformanceConfig.USERS);
    }

    @After
    public void tearDown() throws Exception {
        if (executorService != null) {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }

        if (metricsCollector != null) {
            metricsCollector.printReport();
            metricsCollector.exportToCSV("target/performance-results/weather-api-metrics.csv");
        }
    }

    /**
     * Load Test: Tests system behavior under expected load
     */
    @Test
    public void testCurrentWeatherEndpointUnderLoad() throws InterruptedException {
        System.out.println("Starting Load Test - Current Weather Endpoint");

        List<Future<Response>> futures = new ArrayList<>();
        String[] cities = {"London", "New York", "Tokyo", "Paris", "Sydney", "Berlin", "Mumbai", "Toronto"};

        // Generate load
        for (int i = 0; i < PerformanceConfig.USERS * 10; i++) {
            final String city = cities[i % cities.length];

            Future<Response> future = executorService.submit(() -> {
                metricsCollector.incrementActiveRequests();
                try {
                    Response response = given()
                            .spec(TestConfig.baseSpec())
                            .queryParam("q", city)
                            .when()
                            .get("/current.json");

                    metricsCollector.recordResponse(response);
                    return response;
                } finally {
                    metricsCollector.decrementActiveRequests();
                }
            });

            futures.add(future);

            // Gradual ramp-up: add delay every 10 requests
            if (i % 10 == 0) {
                Thread.sleep(100);
            }
        }

        // Wait for all requests to complete
        for (Future<Response> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                System.err.println("Request failed: " + e.getMessage());
            }
        }

        // Validate SLA
        PerformanceMetricsCollector.PerformanceReport report = metricsCollector.getReport();
        assertTrue("P95 response time exceeds SLA threshold",
                report.p95ResponseTime <= PerformanceConfig.RESPONSE_TIME_P95_THRESHOLD);
        assertTrue("Success rate below threshold",
                report.meetsSLA(
                        PerformanceConfig.RESPONSE_TIME_P95_THRESHOLD,
                        PerformanceConfig.RESPONSE_TIME_P99_THRESHOLD,
                        PerformanceConfig.SUCCESS_RATE_THRESHOLD
                ));
    }

    /**
     * Stress Test: Tests system beyond normal capacity
     */
    @Test
    public void testForecastEndpointStress() throws InterruptedException {
        System.out.println("Starting Stress Test - Forecast Endpoint");

        List<Future<Response>> futures = new ArrayList<>();
        int stressUsers = PerformanceConfig.STRESS_USERS;

        // Generate heavy load
        for (int i = 0; i < stressUsers * 5; i++) {
            Future<Response> future = executorService.submit(() -> {
                metricsCollector.incrementActiveRequests();
                try {
                    Response response = given()
                            .spec(TestConfig.baseSpec())
                            .queryParam("q", "London")
                            .queryParam("days", "3")
                            .when()
                            .get("/forecast.json");

                    metricsCollector.recordResponse(response);
                    return response;
                } finally {
                    metricsCollector.decrementActiveRequests();
                }
            });

            futures.add(future);
        }

        // Wait for completion
        for (Future<Response> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                System.err.println("Request failed: " + e.getMessage());
            }
        }

        PerformanceMetricsCollector.PerformanceReport report = metricsCollector.getReport();
        System.out.println("Stress Test Results - Error Rate: " + report.errorRate + "%");

        // In stress test, we expect some degradation but not complete failure
        assertTrue("Error rate too high even under stress", report.errorRate < 50.0);
    }

    /**
     * Spike Test: Tests system resilience to sudden traffic bursts
     */
    @Test
    public void testSpikeLoad() throws InterruptedException {
        System.out.println("Starting Spike Test - Sudden Traffic Burst");

        // Warm-up period with low load
        for (int i = 0; i < 5; i++) {
            given().spec(TestConfig.baseSpec())
                    .queryParam("q", "London")
                    .get("/current.json");
            Thread.sleep(1000);
        }

        // Sudden spike
        List<Future<Response>> futures = new ArrayList<>();
        for (int i = 0; i < PerformanceConfig.SPIKE_USERS; i++) {
            Future<Response> future = executorService.submit(() -> {
                metricsCollector.incrementActiveRequests();
                try {
                    Response response = given()
                            .spec(TestConfig.baseSpec())
                            .queryParam("q", "Tokyo")
                            .when()
                            .get("/current.json");

                    metricsCollector.recordResponse(response);
                    return response;
                } finally {
                    metricsCollector.decrementActiveRequests();
                }
            });
            futures.add(future);
        }

        // Wait for spike to complete
        for (Future<Response> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                System.err.println("Request failed during spike: " + e.getMessage());
            }
        }

        PerformanceMetricsCollector.PerformanceReport report = metricsCollector.getReport();
        System.out.println("Spike Test - System recovered with error rate: " + report.errorRate + "%");
    }

    /**
     * Endurance Test: Tests system stability over time
     */
    @Test
    public void testEndurance() throws InterruptedException {
        System.out.println("Starting Endurance Test - Sustained Load Over Time");

        int durationSeconds = 60; // 1 minute endurance test
        int requestsPerSecond = 5;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);

        while (System.currentTimeMillis() < endTime) {
            List<Future<Response>> batchFutures = new ArrayList<>();

            for (int i = 0; i < requestsPerSecond; i++) {
                Future<Response> future = executorService.submit(() -> {
                    metricsCollector.incrementActiveRequests();
                    try {
                        Response response = given()
                                .spec(TestConfig.baseSpec())
                                .queryParam("q", "Berlin")
                                .when()
                                .get("/current.json");

                        metricsCollector.recordResponse(response);
                        return response;
                    } finally {
                        metricsCollector.decrementActiveRequests();
                    }
                });
                batchFutures.add(future);
            }

            // Wait for batch to complete
            for (Future<Response> future : batchFutures) {
                try {
                    future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException | TimeoutException e) {
                    System.err.println("Request failed: " + e.getMessage());
                }
            }

            Thread.sleep(1000); // Maintain steady rate
        }

        PerformanceMetricsCollector.PerformanceReport report = metricsCollector.getReport();

        // Validate system remained stable
        assertTrue("System degraded during endurance test",
                report.meetsSLA(
                        PerformanceConfig.RESPONSE_TIME_P95_THRESHOLD,
                        PerformanceConfig.RESPONSE_TIME_P99_THRESHOLD,
                        95.0 // Slightly relaxed for endurance
                ));
    }
}
