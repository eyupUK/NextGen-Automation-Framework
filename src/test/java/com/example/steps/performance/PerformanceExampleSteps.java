package com.example.steps.performance;

import com.example.performance.examples.PerformanceTestingExamples;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.*;

public class PerformanceExampleSteps {

    private Throwable lastError;

    @Given("performance examples are available")
    public void performance_examples_are_available() {
        // Sanity: class should be loadable
        assertNotNull("Examples class not found", new PerformanceTestingExamples());
    }

    @When("I run the simple load performance example")
    public void i_run_the_simple_load_performance_example() {
        try {
            lastError = null;
            PerformanceTestingExamples.simpleLoadTest();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @When("I run the ramp-up performance example")
    public void i_run_the_ramp_up_performance_example() {
        try {
            lastError = null;
            PerformanceTestingExamples.rampUpLoadTest();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @When("I run the heavy load performance example")
    public void i_run_the_heavy_load_performance_example() {
        try {
            lastError = null;
            PerformanceTestingExamples.multiEndpointTest();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @When("I run the spike load performance example")
    public void i_run_the_spike_load_performance_example() {
        try {
            lastError = null;
            PerformanceTestingExamples.concurrentUsersSimulation();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @When("I run the stress test performance example")
    public void i_run_the_stress_test_performance_example() {
        try {
            lastError = null;
            PerformanceTestingExamples.throughputTest();
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @When("I run the endurance test performance example")
    public void i_run_the_endurance_test_performance_example() {
        try {
            lastError = null;
            PerformanceTestingExamples.throughputTest(); // Reuse throughputTest for endurance
        } catch (Throwable t) {
            lastError = t;
        }
    }

    @Then("it completes successfully")
    public void it_completes_successfully() {
        if (lastError != null) {
            lastError.printStackTrace();
        }
        assertNull("Performance example failed: " + (lastError == null ? "" : lastError.getMessage()), lastError);
    }

}
