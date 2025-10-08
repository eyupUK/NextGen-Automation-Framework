package com.example.steps;

import com.example.config.Driver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Hooks {

    /**
     * Private static WebDriver instance to hold the driver instance.
     */
    private WebDriver driver;


    /**
     * Initializes the WebDriver instance, maximizes the window, and sets an implicit wait.
     */
    @Before()
    public void setUp(Scenario scenario) {
        boolean isApi = scenario.getSourceTagNames().contains("@api");
        if (!isApi) {
            driver = Driver.get();
            driver.manage().window().maximize();
//            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        }
    }

    /**
     * Tears down the test environment by closing the WebDriver instance.
     * If a test fails, it attaches a screenshot to the scenario.
     * If the test completes successfully, it attaches the screenshot by titling "Proof_of_Expected_Result".
     *
     * @param scenario The Cucumber scenario object.
     */
    @After
    public void tearDown(Scenario scenario) {
        boolean isApi = scenario.getSourceTagNames().contains("@api");
        if (!isApi && driver != null) {
            if (scenario.isFailed()) {
                final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failure_Screenshot");
            } else {
                final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Proof_of_Expected_Result");
                System.out.println("Test completed successfully");
            }
            Driver.closeDriver();
        }
    }
}
