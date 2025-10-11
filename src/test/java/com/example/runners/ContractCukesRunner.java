package com.example.runners;


import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber6jvm.AllureCucumber6Jvm",
                "json:target/contract-cucumber.json",
                "html:target/contract-cucumber-report.html",
                "rerun:target/contract-rerun.txt",
                "me.jvt.cucumber.report.PrettyReports"
        },
        features = "src/test/resources/features/contract", // only contract tests will be executed
        glue = "com/example/steps",
        dryRun = false,
        tags = ""
)
public class ContractCukesRunner {
}
