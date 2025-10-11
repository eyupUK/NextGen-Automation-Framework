package com.example.steps.contract;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class ContractSteps {

    private Result lastResult;

    @Given("the Weather API consumer contracts are available")
    public void the_weather_api_consumer_contracts_are_available() {
        // No-op sanity step; Pact tests are compiled under com.example.contract
    }


    @Then("the contract verification should pass")
    public void the_contract_verification_should_pass() {
        if (lastResult == null) {
            Assert.fail("No test result captured. Ensure the When step executed.");
        }
        if (!lastResult.wasSuccessful()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Contract test failed. Failures: ");
            lastResult.getFailures().forEach(f -> sb.append("\n- ").append(f.getTestHeader()).append(": ").append(f.getMessage()));
            Assert.fail(sb.toString());
        }
    }

    @When("I run the contract test {string}")
    public void iRunTheContractTest(String testClassSimpleName) throws ClassNotFoundException {
        String fqcn = "com.example.contract." + testClassSimpleName;
        Class<?> clazz = Class.forName(fqcn);
        lastResult = JUnitCore.runClasses(clazz);
    }
}
