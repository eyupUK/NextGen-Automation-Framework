package com.example.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "pretty", "io.qameta.allure.cucumber6jvm.AllureCucumber6Jvm",
                "json:target/cucumber-accessibility.json",
                "html:target/cucumber-accessibility-report.html",
                "rerun:target/rerun-accessibility.txt",
                "me.jvt.cucumber.report.PrettyReports"
        },
        features = "src/test/resources/features/accessibility", // only accessibility tests will be executed
        glue = "com/example/steps",
        tags = "",
        dryRun = false
)
public class AccessibilityCukesRunner {
}

