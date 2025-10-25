package com.example.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    static {
        // Ensure the rerun file exists (Cucumber will attempt to read '@target/rerun.txt')
        try {
            Path rerun = Paths.get("target", "rerun.txt");
            if (!Files.exists(rerun)) {
                if (!Files.exists(rerun.getParent())) {
                    Files.createDirectories(rerun.getParent());
                }
                Files.createFile(rerun);
            }
        } catch (IOException ignored) {
            // Best-effort: if we cannot create it, let Cucumber handle missing file (it may still fail)
        }
    }
}
