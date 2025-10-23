package com.example.steps;

import com.example.config.Driver;
import com.example.util.OAuthConfig;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

public class Hooks {

    // Provide access to current Scenario without adding parameters to steps
    private static final ThreadLocal<Scenario> CURRENT_SCENARIO = new ThreadLocal<>();
    public static Scenario getScenario() { return CURRENT_SCENARIO.get(); }

    /**
     * Private static WebDriver instance to hold the driver instance.
     */
    private WebDriver driver;

    private static String mask(String value) {
        if (value == null || value.isBlank()) return "<empty>";
        int len = value.length();
        if (len <= 4) return "***"; // too short to reveal
        int prefix = Math.min(4, len / 2);
        int suffix = Math.min(2, len - prefix);
        StringBuilder sb = new StringBuilder();
        sb.append(value, 0, prefix).append("***")
          .append(value.substring(len - suffix));
        return sb.toString();
    }

    private static String hostFrom(String url) {
        try {
            if (url == null || url.isBlank()) return "<unset>";
            return URI.create(url).getHost();
        } catch (Exception e) {
            return "<invalid>";
        }
    }

    private static String predictTokenPath(Collection<String> tags, String tokenUrl) {
        String url = tokenUrl == null ? "" : tokenUrl.toLowerCase();
        boolean spotifyTag = tags.stream().anyMatch(t -> t.equalsIgnoreCase("@spotify"));
        if (spotifyTag || url.contains("accounts.spotify.com")) return "OPAQUE (Spotify)";
        if (url.contains("duendesoftware.com") || url.contains("identityserver")) return "JWT (IdentityServer)";
        return "UNKNOWN (will auto-detect)";
    }

    // Overload to be compatible with older Cucumber APIs that return Set<String>
    private static String predictTokenPath(Set<String> tags, String tokenUrl) {
        return predictTokenPath((Collection<String>) tags, tokenUrl);
    }

    /**
     * Initializes the WebDriver instance, maximizes the window, and sets an implicit wait.
     */
    @Before()
    public void setUp(Scenario scenario) {
        CURRENT_SCENARIO.set(scenario);

        // Small startup log for OAuth scenarios
        if (scenario.getSourceTagNames().contains("@oauth")) {
            String tokenUrl = OAuthConfig.tokenUrl();
            String clientId = OAuthConfig.clientId();
            String scope = OAuthConfig.scope();
            String host = hostFrom(tokenUrl);
            String pathPrediction = predictTokenPath(scenario.getSourceTagNames(), tokenUrl);

            StringBuilder log = new StringBuilder()
                    .append("[OAuth Startup] Provider host=").append(host)
                    .append(", token_url=").append(tokenUrl == null ? "<unset>" : tokenUrl)
                    .append(", client_id=").append(mask(clientId))
                    .append(", scope=").append((scope == null || scope.isBlank()) ? "<empty>" : scope)
                    .append(", predicted_token_type=").append(pathPrediction);

            // Print and attach to scenario
            System.out.println(log);
            try {
                scenario.attach(log.toString(), "text/plain", "OAuth Startup");
            } catch (Throwable ignored) { /* attach may vary by formatter; best-effort */ }
        }

        boolean isUi = scenario.getSourceTagNames().contains("@ui");
        if (isUi) {
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
        boolean isUi = scenario.getSourceTagNames().contains("@ui");
        if (isUi && driver != null) {
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
        CURRENT_SCENARIO.remove();
    }
}
