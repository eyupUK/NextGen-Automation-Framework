package com.example.runners;

import com.example.performance.junit.tests.FakeStorePerformanceTest;
import com.example.performance.junit.tests.WeatherApiPerformanceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * JUnit suite runner for performance test suites.
 * Run with:
 *   mvn -Dtest=PerformanceJUnitRunner test
 * Ensure WEATHER_API_KEY and any perf.* configs are set as needed.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        com.example.performance.config.PerformanceConfigInitTest.class,
        WeatherApiPerformanceTest.class,
        FakeStorePerformanceTest.class
})
public class PerformanceJUnitRunner {
}
