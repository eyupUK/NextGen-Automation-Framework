package com.example.performance.examples;

import com.example.config.TestConfig;
import com.example.performance.utils.PerformanceMetricsCollector;
import com.example.performance.utils.LoadGenerator;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;

/**
 * Performance Testing Examples
 *
 * Demonstrates various performance testing patterns and best practices
 * These examples can be used as templates for creating custom performance tests
 */
public class PerformanceTestingExamples {

    /**
     * Example 1: Simple Load Test
     * Tests a single endpoint with multiple concurrent users
     */
    public static void simpleLoadTest() throws InterruptedException {
        System.out.println("=== Example 1: Simple Load Test ===\n");

        PerformanceMetricsCollector metrics = new PerformanceMetricsCollector("Simple Load Test");
        LoadGenerator loadGen = new LoadGenerator(10);
        loadGen.start();

        int totalRequests = 100;
        CountDownLatch latch = new CountDownLatch(totalRequests);

        for (int i = 0; i < totalRequests; i++) {
            loadGen.execute(() -> {
                try {
                    metrics.incrementActiveRequests();
                    Response response = given()
                            .spec(TestConfig.baseSpec())
                            .queryParam("q", "London")
                            .get("/current.json");

                    metrics.recordResponse(response);
                } finally {
                    metrics.decrementActiveRequests();
                    latch.countDown();
                }
            });
        }

        latch.await();
        loadGen.shutdown();
        metrics.printReport();
    }

    /**
     * Example 2: Ramp-Up Load Test
     * Gradually increases load over time
     */
    public static void rampUpLoadTest() throws InterruptedException {
        System.out.println("=== Example 2: Ramp-Up Load Test ===\n");

        PerformanceMetricsCollector metrics = new PerformanceMetricsCollector("Ramp-Up Test");
        LoadGenerator loadGen = new LoadGenerator(50);
        loadGen.start();

        int maxUsers = 50;
        int rampUpSeconds = 10;
        int requestsPerUser = 5;

        for (int user = 1; user <= maxUsers; user++) {
            final int currentUser = user;

            loadGen.execute(() -> {
                for (int req = 0; req < requestsPerUser; req++) {
                    try {
                        metrics.incrementActiveRequests();
                        Response response = given()
                                .spec(TestConfig.baseSpec())
                                .queryParam("q", "Paris")
                                .get("/current.json");

                        metrics.recordResponse(response);
                        Thread.sleep(1000); // Think time
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        metrics.decrementActiveRequests();
                    }
                }
            });

            // Gradual ramp-up
            Thread.sleep((rampUpSeconds * 1000L) / maxUsers);
        }

        Thread.sleep(5000); // Wait for completion
        loadGen.shutdown();
        metrics.printReport();
    }

    /**
     * Example 3: Multi-Endpoint Test
     * Tests multiple endpoints with different load distributions
     */
    public static void multiEndpointTest() throws InterruptedException {
        System.out.println("=== Example 3: Multi-Endpoint Test ===\n");

        PerformanceMetricsCollector currentMetrics = new PerformanceMetricsCollector("Current Weather");
        PerformanceMetricsCollector forecastMetrics = new PerformanceMetricsCollector("Forecast");

        LoadGenerator loadGen = new LoadGenerator(30);
        loadGen.start();

        CountDownLatch latch = new CountDownLatch(100);

        // 70% current weather requests
        for (int i = 0; i < 70; i++) {
            loadGen.execute(() -> {
                try {
                    currentMetrics.incrementActiveRequests();
                    Response response = given()
                            .spec(TestConfig.baseSpec())
                            .queryParam("q", "Tokyo")
                            .get("/current.json");

                    currentMetrics.recordResponse(response);
                } finally {
                    currentMetrics.decrementActiveRequests();
                    latch.countDown();
                }
            });
        }

        // 30% forecast requests
        for (int i = 0; i < 30; i++) {
            loadGen.execute(() -> {
                try {
                    forecastMetrics.incrementActiveRequests();
                    Response response = given()
                            .spec(TestConfig.baseSpec())
                            .queryParam("q", "Tokyo")
                            .queryParam("days", "3")
                            .get("/forecast.json");

                    forecastMetrics.recordResponse(response);
                } finally {
                    forecastMetrics.decrementActiveRequests();
                    latch.countDown();
                }
            });
        }

        latch.await();
        loadGen.shutdown();

        System.out.println("\nCurrent Weather Endpoint:");
        currentMetrics.printReport();

        System.out.println("\nForecast Endpoint:");
        forecastMetrics.printReport();
    }

    /**
     * Example 4: Concurrent Users Simulation
     * Simulates realistic user behavior with think time
     */
    public static void concurrentUsersSimulation() throws InterruptedException {
        System.out.println("=== Example 4: Concurrent Users Simulation ===\n");

        PerformanceMetricsCollector metrics = new PerformanceMetricsCollector("User Journey");
        LoadGenerator loadGen = new LoadGenerator(20);
        loadGen.start();

        int numberOfUsers = 20;
        CountDownLatch latch = new CountDownLatch(numberOfUsers);
        String[] cities = {"London", "Paris", "Tokyo", "New York", "Sydney"};

        for (int user = 0; user < numberOfUsers; user++) {
            final int userId = user;

            loadGen.execute(() -> {
                try {
                    // User journey: 3 requests with think time
                    for (int step = 0; step < 3; step++) {
                        metrics.incrementActiveRequests();
                        String city = cities[userId % cities.length];

                        Response response = given()
                                .spec(TestConfig.baseSpec())
                                .queryParam("q", city)
                                .get("/current.json");

                        metrics.recordResponse(response);
                        metrics.decrementActiveRequests();

                        // Think time: 1-3 seconds
                        Thread.sleep(1000 + (long)(Math.random() * 2000));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        loadGen.shutdown();
        metrics.printReport();
    }

    /**
     * Example 5: Throughput Test
     * Measures maximum requests per second
     */
    public static void throughputTest() throws InterruptedException {
        System.out.println("=== Example 5: Throughput Test ===\n");

        PerformanceMetricsCollector metrics = new PerformanceMetricsCollector("Throughput Test");
        LoadGenerator loadGen = new LoadGenerator(100);
        loadGen.start();

        int durationSeconds = 30;
        AtomicInteger requestCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);

        while (System.currentTimeMillis() < endTime) {
            loadGen.execute(() -> {
                try {
                    metrics.incrementActiveRequests();
                    Response response = given()
                            .spec(TestConfig.baseSpec())
                            .queryParam("q", "Berlin")
                            .get("/current.json");

                    metrics.recordResponse(response);
                    requestCount.incrementAndGet();
                } finally {
                    metrics.decrementActiveRequests();
                }
            });
        }

        Thread.sleep(5000); // Wait for completion
        loadGen.shutdown();

        double actualDuration = (System.currentTimeMillis() - startTime) / 1000.0;
        double throughput = requestCount.get() / actualDuration;

        System.out.println("Total requests: " + requestCount.get());
        System.out.println("Duration: " + String.format("%.2f", actualDuration) + " seconds");
        System.out.println("Achieved throughput: " + String.format("%.2f", throughput) + " req/sec\n");

        metrics.printReport();
    }

    /**
     * Main method to run all examples
     */
    public static void main(String[] args) {
        try {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║     Performance Testing Examples Demonstration        ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

            // Run examples (uncomment to run specific examples)

            simpleLoadTest();
            System.out.println("\n" + "─".repeat(60) + "\n");

            // rampUpLoadTest();
            // System.out.println("\n" + "─".repeat(60) + "\n");

            // multiEndpointTest();
            // System.out.println("\n" + "─".repeat(60) + "\n");

            // concurrentUsersSimulation();
            // System.out.println("\n" + "─".repeat(60) + "\n");

            // throughputTest();

            System.out.println("\n✓ All examples completed successfully!");

        } catch (Exception e) {
            System.err.println("Error running examples: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

