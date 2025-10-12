package com.example.runners;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Programmatic runner for Gatling simulations.
 *
 * Usage examples:
 * - mvn -DskipTests test-compile && mvn -Dtest=GatlingTestsRunner test
 * - mvn -Dgatling.simulationClass=com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation \
 *      -Dtest=GatlingTestsRunner test
 *
 * You can also run the main method from your IDE.
 */
public class GatlingTestsRunner {

    private static final String[] DEFAULT_SIMULATIONS = new String[] {
        "com.example.performance.gatling.simulations.WeatherApiPerformanceSimulation",
        "com.example.performance.gatling.simulations.EcommerceApiPerformanceSimulation"
    };

    private static String resultsDir() {
        return System.getProperty("gatling.resultsFolder", "target/gatling-results");
    }

    private static String resourcesDir() {
        return System.getProperty("gatling.resourcesFolder", "src/test/resources");
    }

    private static String binariesDir() {
        return System.getProperty("gatling.binariesFolder", "target/test-classes");
    }

    private static void runSimulation(String simulationClass) {
        System.out.println("Running Gatling simulation: " + simulationClass);
        GatlingPropertiesBuilder props = new GatlingPropertiesBuilder()
            .simulationClass(simulationClass)
            .resultsDirectory(resultsDir())
            .resourcesDirectory(resourcesDir())
            .binariesDirectory(binariesDir());

        // Ensure results directory exists
        try {
            Files.createDirectories(Path.of(resultsDir()));
        } catch (Exception ignored) {}

        Gatling.fromMap(props.build());
        System.out.println("Completed: " + simulationClass + " \nResults: " + resultsDir());
    }

    /**
     * JUnit entry point so this can be invoked with Surefire if desired.
     * Select a simulation with -Dgatling.simulationClass, otherwise runs defaults sequentially.
     */
    @Test
    public void runGatlingViaJUnit() {
        String sim = System.getProperty("gatling.simulationClass", "").trim();
        List<String> simulations = new ArrayList<>();
        if (!sim.isEmpty()) {
            simulations.add(sim);
        } else {
            simulations.addAll(Arrays.asList(DEFAULT_SIMULATIONS));
        }
        simulations.forEach(GatlingTestsRunner::runSimulation);
    }

    /**
     * Main entry point to run from IDE/CLI without Maven plugin.
     * Accepts optional first arg as the simulation class; falls back to system property, then defaults.
     */
    public static void main(String[] args) {
        String simArg = (args != null && args.length > 0) ? args[0] : null;
        String simProp = System.getProperty("gatling.simulationClass", "").trim();

        List<String> simulations = new ArrayList<>();
        if (simArg != null && !simArg.isBlank()) {
            simulations.add(simArg);
        } else if (!simProp.isEmpty()) {
            simulations.add(simProp);
        } else {
            simulations.addAll(Arrays.asList(DEFAULT_SIMULATIONS));
        }

        simulations.forEach(GatlingTestsRunner::runSimulation);
    }
}
