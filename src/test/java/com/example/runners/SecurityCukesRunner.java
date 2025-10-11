package com.example.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "pretty", "io.qameta.allure.cucumber6jvm.AllureCucumber6Jvm",
                "json:target/security-cucumber.json",
                "html:target/security-cucumber-report.html",
                "rerun:target/security-rerun.txt",
                "me.jvt.cucumber.report.PrettyReports"
        },
        features = "src/test/resources/features/security", // only security tests will be executed
        glue = "com/example/steps",
        dryRun = false,
        tags = ""
)
public class SecurityCukesRunner {
}

