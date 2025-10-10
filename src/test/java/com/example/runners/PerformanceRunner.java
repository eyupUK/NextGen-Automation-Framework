package com.example.runners;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * JUnit suite runner for performance test suites.
 * Run with:
 *   mvn -Dtest=PerformanceRunner test
 * Ensure WEATHER_API_KEY and any perf.* configs are set as needed.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        com.example.performance.tests.WeatherApiPerformanceTest.class,
        com.example.performance.config.PerformanceConfigInitTest.class
})
public class PerformanceRunner {
}
