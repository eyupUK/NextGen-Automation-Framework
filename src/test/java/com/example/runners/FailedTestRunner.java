package com.example.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * This class is a test runner for Cucumber tests. It is responsible for executing the Cucumber tests and generating an HTML report of failed tests.
 *
 * @param plugin The plugin to generate the HTML report of failed tests.
 * @param features The location of the Cucumber feature files.
 * @param glue The package containing the step definitions for the Cucumber tests.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"html:target/failed-html-report"},
        features = "@target/rerun.txt",
        glue = "com/example/steps"
)
public class FailedTestRunner {
}
