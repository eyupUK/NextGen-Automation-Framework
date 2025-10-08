package com.example.performance.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Load Generator Utility
 *
 * Provides utilities for generating concurrent load in performance tests
 * Follows industry best practices for thread management
 */
public class LoadGenerator {

    private final int threadPoolSize;
    private ExecutorService executorService;

    public LoadGenerator(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * Start the load generator
     */
    public void start() {
        executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * Execute a task with the load generator
     */
    public void execute(Runnable task) {
        if (executorService == null || executorService.isShutdown()) {
            throw new IllegalStateException("Load generator is not started");
        }
        executorService.submit(task);
    }

    /**
     * Shutdown the load generator gracefully
     */
    public void shutdown() throws InterruptedException {
        if (executorService != null) {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
    }

    /**
     * Get the executor service (for advanced usage)
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }
}

