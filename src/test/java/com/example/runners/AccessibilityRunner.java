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
        features = "src/test/resources/features",
        glue = "com/example/steps",
        tags = "@accessibility",
        dryRun = false
)
public class AccessibilityRunner {
}

