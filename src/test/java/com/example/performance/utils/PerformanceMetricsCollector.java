package com.example.performance.utils;

import io.restassured.response.Response;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Collects response timings and outcomes for performance tests.
 * All durations are stored in MILLISECONDS.
 */
public class PerformanceMetricsCollector {

    public static final class PerformanceReport {
        public final String name;
        public final int totalRequests;
        public final int successCount;
        public final int errorCount;
        /** Percentage [0..100] */
        public final double errorRate;
        /** ms */
        public final double meanResponseTime;
        /** ms */
        public final double medianResponseTime;
        /** ms */
        public final double p95ResponseTime;
        /** ms */
        public final double p99ResponseTime;
        /** ms */
        public final double maxResponseTime;
        public final int activeRequests;

        private PerformanceReport(
                String name,
                int totalRequests,
                int successCount,
                int errorCount,
                double errorRate,
                double meanResponseTime,
                double medianResponseTime,
                double p95ResponseTime,
                double p99ResponseTime,
                double maxResponseTime,
                int activeRequests) {
            this.name = name;
            this.totalRequests = totalRequests;
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.errorRate = errorRate;
            this.meanResponseTime = meanResponseTime;
            this.medianResponseTime = medianResponseTime;
            this.p95ResponseTime = p95ResponseTime;
            this.p99ResponseTime = p99ResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.activeRequests = activeRequests;
        }

        public boolean meetsSLA(double p95Ms, double p99Ms, double successRatePercent) {
            double successRate = totalRequests == 0 ? 0.0 : (successCount * 100.0 / totalRequests);
            return p95ResponseTime <= p95Ms && p99ResponseTime <= p99Ms && successRate >= successRatePercent;
        }
    }

    private final String name;
    private final ConcurrentLinkedQueue<Long> durationsMs = new ConcurrentLinkedQueue<>();
    private final LongAdder success = new LongAdder();
    private final LongAdder errors = new LongAdder();
    private final LongAdder active = new LongAdder();

    public PerformanceMetricsCollector(String name) {
        this.name = name;
    }

    /** Prefer this method from your tests. Stores response time in MILLISECONDS. */
    public void recordResponse(Response response) {
        long millis = response.getTimeIn(TimeUnit.MILLISECONDS);
        durationsMs.add(millis);

        int code = response.getStatusCode();
        if (code >= 200 && code < 400) {
            success.increment();
        } else {
            errors.increment();
        }
    }

    /** If you must record a duration measured with nanoTime(), call this and we convert nanos -> ms. */
    public void recordDurationNanos(long nanos, int httpStatus) {
        long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
        durationsMs.add(millis);
        if (httpStatus >= 200 && httpStatus < 400) success.increment(); else errors.increment();
    }

    public void incrementActiveRequests() { active.increment(); }
    public void decrementActiveRequests() { active.decrement(); }

    public PerformanceReport getReport() {
        // Snapshot durations into a list
        List<Long> list = new ArrayList<>(durationsMs);
        Collections.sort(list);

        int n = list.size();
        double mean = 0.0, median = 0.0, p95 = 0.0, p99 = 0.0, max = 0.0;
        if (n > 0) {
            long sum = 0L;
            for (long d : list) sum += d;
            mean = sum / (double) n;
            median = percentile(list, 50);
            p95 = percentile(list, 95);
            p99 = percentile(list, 99);
            max = list.get(n - 1);
        }

        int ok = success.intValue();
        int err = errors.intValue();
        int total = ok + err;
        double errRate = total == 0 ? 0.0 : (err * 100.0 / total);

        return new PerformanceReport(
                name, total, ok, err, errRate, mean, median, p95, p99, max, active.intValue()
        );
    }

    private static double percentile(List<Long> sortedMillis, int pct) {
        if (sortedMillis.isEmpty()) return 0.0;
        double rank = Math.ceil((pct / 100.0) * sortedMillis.size());
        int idx = Math.max(0, Math.min(sortedMillis.size() - 1, (int) rank - 1));
        return sortedMillis.get(idx);
    }

    public void printReport() {
        PerformanceReport r = getReport();
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("PERFORMANCE TEST REPORT: " + r.name);
        System.out.println("================================================================================");
        System.out.printf(Locale.ROOT, "Total Requests: %d%n", r.totalRequests);
        System.out.printf(Locale.ROOT, "Total Errors: %d (%.2f%%)%n", r.errorCount, r.errorRate);
        System.out.printf(Locale.ROOT, "Mean Response Time: %.2f ms%n", r.meanResponseTime);
        System.out.printf(Locale.ROOT, "Median Response Time: %.2f ms%n", r.medianResponseTime);
        System.out.printf(Locale.ROOT, "95th Percentile: %.2f ms%n", r.p95ResponseTime);
        System.out.printf(Locale.ROOT, "99th Percentile: %.2f ms%n", r.p99ResponseTime);
        System.out.printf(Locale.ROOT, "Max Response Time: %.2f ms%n", r.maxResponseTime);
        double throughput = (r.totalRequests <= 0 || r.meanResponseTime <= 0)
                ? 0.0
                : 1000.0 / r.meanResponseTime; // simplistic per-thread; keep if you like
        System.out.printf(Locale.ROOT, "Throughput: %.2f req/sec%n", throughput);
        System.out.printf(Locale.ROOT, "Active Requests: %d%n", r.activeRequests);
        System.out.println("================================================================================");
        System.out.println();
    }

//    public void exportToCSV(String path) {
//        PerformanceReport r = getReport();
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
//            bw.write("name,totalRequests,successCount,errorCount,errorRate,meanMs,medianMs,p95Ms,p99Ms,maxMs,active\n");
//            bw.write(String.format(
//                    Locale.ROOT,
//                    "%s,%d,%d,%d,%.4f,%.3f,%.3f,%.3f,%.3f,%.3f,%d%n",
//                    r.name, r.totalRequests, r.successCount, r.errorCount, r.errorRate,
//                    r.meanResponseTime, r.medianResponseTime, r.p95ResponseTime, r.p99ResponseTime,
//                    r.maxResponseTime, r.activeRequests
//            ));
//        } catch (IOException e) {
//            System.err.println("CSV export failed: " + e.getMessage());
//        }
//    }

    public void exportToCSV(String path) {
        PerformanceReport r = getReport();
        java.nio.file.Path p = java.nio.file.Paths.get(path);
        try {
            java.nio.file.Path parent = p.getParent();
            if (parent != null) {
                java.nio.file.Files.createDirectories(parent); // <-- ensure dir exists
            }
            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(p.toFile()))) {
                bw.write("name,totalRequests,successCount,errorCount,errorRate,meanMs,medianMs,p95Ms,p99Ms,maxMs,active\n");
                bw.write(String.format(
                        java.util.Locale.ROOT,
                        "%s,%d,%d,%d,%.4f,%.3f,%.3f,%.3f,%.3f,%.3f,%d%n",
                        r.name, r.totalRequests, r.successCount, r.errorCount, r.errorRate,
                        r.meanResponseTime, r.medianResponseTime, r.p95ResponseTime, r.p99ResponseTime,
                        r.maxResponseTime, r.activeRequests
                ));
            }
        } catch (Exception e) {
            System.err.println("CSV export failed: " + e.getMessage());
        }
    }

}
