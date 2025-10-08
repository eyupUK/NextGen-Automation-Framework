package com.example.performance.utils;

import com.codahale.metrics.*;
import io.restassured.response.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Performance Metrics Collector
 *
 * Collects and reports performance metrics using Dropwizard Metrics library
 * Industry best practices:
 * - Tracks response times, throughput, error rates
 * - Calculates percentiles (p50, p95, p99)
 * - Generates CSV reports for further analysis
 */
public class PerformanceMetricsCollector {

    private final MetricRegistry metrics = new MetricRegistry();
    private final Timer responseTimer;
    private final Meter requestMeter;
    private final Meter errorMeter;
    private final Counter activeRequests;
    private final String testName;

    public PerformanceMetricsCollector(String testName) {
        this.testName = testName;
        this.responseTimer = metrics.timer("response-time");
        this.requestMeter = metrics.meter("requests");
        this.errorMeter = metrics.meter("errors");
        this.activeRequests = metrics.counter("active-requests");
    }

    /**
     * Record a successful request
     */
    public void recordRequest(long responseTimeMs) {
        responseTimer.update(responseTimeMs, TimeUnit.MILLISECONDS);
        requestMeter.mark();
    }

    /**
     * Record a failed request
     */
    public void recordError(long responseTimeMs) {
        responseTimer.update(responseTimeMs, TimeUnit.MILLISECONDS);
        requestMeter.mark();
        errorMeter.mark();
    }

    /**
     * Record request from REST Assured Response
     */
    public void recordResponse(Response response) {
        long responseTime = response.getTime(); // Already in milliseconds from REST Assured
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            recordRequest(responseTime);
        } else {
            recordError(responseTime);
        }
    }

    /**
     * Increment active requests counter
     */
    public void incrementActiveRequests() {
        activeRequests.inc();
    }

    /**
     * Decrement active requests counter
     */
    public void decrementActiveRequests() {
        activeRequests.dec();
    }

    /**
     * Get performance summary report
     */
    public PerformanceReport getReport() {
        Snapshot snapshot = responseTimer.getSnapshot();

        return new PerformanceReport(
                testName,
                requestMeter.getCount(),
                errorMeter.getCount(),
                calculateErrorRate(),
                snapshot.getMean(),
                snapshot.getMedian(),
                snapshot.get95thPercentile(),
                snapshot.get99thPercentile(),
                snapshot.getMax(),
                requestMeter.getMeanRate(),
                activeRequests.getCount()
        );
    }

    /**
     * Calculate error rate percentage
     */
    private double calculateErrorRate() {
        long totalRequests = requestMeter.getCount();
        if (totalRequests == 0) return 0.0;
        return (errorMeter.getCount() * 100.0) / totalRequests;
    }

    /**
     * Print detailed report to console
     */
    public void printReport() {
        PerformanceReport report = getReport();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PERFORMANCE TEST REPORT: " + testName);
        System.out.println("=".repeat(80));
        System.out.println(report);
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * Export metrics to CSV file
     */
    public void exportToCSV(String filePath) throws IOException {
        PerformanceReport report = getReport();
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file, true)) {
            // Write header if file is new
            if (file.length() == 0) {
                writer.write("Test Name,Total Requests,Total Errors,Error Rate %,Mean (ms)," +
                        "Median (ms),P95 (ms),P99 (ms),Max (ms),Throughput (req/s)\n");
            }

            writer.write(String.format(Locale.US,
                    "%s,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                    report.testName,
                    report.totalRequests,
                    report.totalErrors,
                    report.errorRate,
                    report.meanResponseTime,
                    report.medianResponseTime,
                    report.p95ResponseTime,
                    report.p99ResponseTime,
                    report.maxResponseTime,
                    report.throughput
            ));
        }
    }

    /**
     * Start reporting metrics to console at regular intervals
     */
    public void startConsoleReporter(int periodSeconds) {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(periodSeconds, TimeUnit.SECONDS);
    }

    /**
     * Performance Report Data Class
     */
    public static class PerformanceReport {
        public final String testName;
        public final long totalRequests;
        public final long totalErrors;
        public final double errorRate;
        public final double meanResponseTime;
        public final double medianResponseTime;
        public final double p95ResponseTime;
        public final double p99ResponseTime;
        public final double maxResponseTime;
        public final double throughput;
        public final long activeRequests;

        public PerformanceReport(String testName, long totalRequests, long totalErrors,
                                 double errorRate, double meanResponseTime, double medianResponseTime,
                                 double p95ResponseTime, double p99ResponseTime, double maxResponseTime,
                                 double throughput, long activeRequests) {
            this.testName = testName;
            this.totalRequests = totalRequests;
            this.totalErrors = totalErrors;
            this.errorRate = errorRate;
            this.meanResponseTime = meanResponseTime;
            this.medianResponseTime = medianResponseTime;
            this.p95ResponseTime = p95ResponseTime;
            this.p99ResponseTime = p99ResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.throughput = throughput;
            this.activeRequests = activeRequests;
        }

        @Override
        public String toString() {
            return String.format(
                    "Total Requests: %d\n" +
                    "Total Errors: %d (%.2f%%)\n" +
                    "Mean Response Time: %.2f ms\n" +
                    "Median Response Time: %.2f ms\n" +
                    "95th Percentile: %.2f ms\n" +
                    "99th Percentile: %.2f ms\n" +
                    "Max Response Time: %.2f ms\n" +
                    "Throughput: %.2f req/sec\n" +
                    "Active Requests: %d",
                    totalRequests, totalErrors, errorRate,
                    meanResponseTime, medianResponseTime,
                    p95ResponseTime, p99ResponseTime, maxResponseTime,
                    throughput, activeRequests
            );
        }

        /**
         * Check if performance meets SLA thresholds
         */
        public boolean meetsSLA(double maxP95, double maxP99, double minSuccessRate) {
            boolean p95Met = p95ResponseTime <= maxP95;
            boolean p99Met = p99ResponseTime <= maxP99;
            boolean successRateMet = (100.0 - errorRate) >= minSuccessRate;

            return p95Met && p99Met && successRateMet;
        }
    }
}
