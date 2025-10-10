package com.example.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber6jvm.AllureCucumber6Jvm",
                "json:target/perf-cucumber.json",
                "html:target/perf-cucumber-report.html",
                "rerun:target/perf-rerun.txt",
                "me.jvt.cucumber.report.PrettyReports"
        },
        features = "src/test/resources/features",
        glue = "com/example/steps",
        dryRun = false,
        tags = "@performance"
)
public class PerformanceCukesRunner {
}

