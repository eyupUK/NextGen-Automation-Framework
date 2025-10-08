package com.example.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * This class is a JUnit runner for Cucumber tests. It is annotated with {@link RunWith} and {@link CucumberOptions} annotations.
 * The {@link CucumberOptions} annotation specifies the configuration for the Cucumber test runner.
 *
 * @author eyupUK
 * @version 1.0
 */
@RunWith(Cucumber.class)
@CucumberOptions(

        /**
         * The {@code plugin} attribute specifies the plugins to be used for generating reports and other artifacts.
         * The following plugins are configured:
         *     {@code pretty}: Prints the feature file and step definitions to the console.
         *     {@code io.qameta.allure.cucumber6jvm.AllureCucumber6Jvm}: Generates Allure reports.
         *     {@code json:target/cucumber.json}: Generates a JSON report in the specified directory.
         *     {@code html:target/cucumber-report.html}: Generates an HTML report in the specified directory.
         *     {@code rerun:target/rerun.txt}: Generates a rerun.txt file that can be used to rerun failed scenarios.
         *     {@code me.jvt.cucumber.report.PrettyReports}: Generates a pretty report.
         *
        */
        plugin = {
                "pretty", "io.qameta.allure.cucumber6jvm.AllureCucumber6Jvm",
                "json:target/cucumber.json",
                "html:target/cucumber-report.html",
                "rerun:target/rerun.txt",
                "me.jvt.cucumber.report.PrettyReports"
        },
        features = "src/test/resources/features",
        glue = "com/example/steps",
        dryRun = false,
        tags = "@api"
)
public class CukesRunner {
}