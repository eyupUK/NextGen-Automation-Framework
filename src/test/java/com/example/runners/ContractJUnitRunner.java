package com.example.runners;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * JUnit suite runner for performance test suites.
 * Run with:
 *   mvn -Dtest=ContractJUnitRunner test
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        com.example.contract.WeatherApiConsumerPactTest.class,
        com.example.contract.WeatherApiBulkRequestPactTest.class,
        com.example.contract.WeatherApiCoordinatesPactTest.class,
        com.example.contract.WeatherApiErrorsPactTest.class,
        com.example.contract.WeatherApiForecastPactTest.class,
        com.example.contract.WeatherApiInvalidLocationPactTest.class,
        com.example.contract.WeatherApiZipCodePactTest.class
})
public class ContractJUnitRunner {
}
