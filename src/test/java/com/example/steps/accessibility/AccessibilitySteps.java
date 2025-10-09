package com.example.steps.accessibility;


import com.example.pages.LoginPage;
import com.example.config.Driver;
import com.example.util.ConfigurationReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;

import com.deque.html.axecore.selenium.AxeBuilder;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.google.gson.Gson;
import com.example.steps.Hooks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

// Additional page objects for navigation
import com.example.pages.InventoryPage;
import com.example.pages.CartPage;
import com.example.pages.CheckoutInfoPage;
import com.example.pages.CheckoutOverviewPage;

public class AccessibilitySteps {

    private final WebDriver driver = Driver.get();
    private Results lastResults;

    @Given("I open the SauceDemo login page")
    public void i_open_the_saucedemo_login_page() {
        new LoginPage().open();
    }

    @Given("I login to SauceDemo as standard user")
    public void i_login_to_saucedemo_as_standard_user() {
        String user = ConfigurationReader.get("STANDARD_USER");
        String pwd = ConfigurationReader.get("STANDARD_PASSWORD");
        new LoginPage().open().loginAsAStandartUser(user, pwd);
    }

    @And("I open the cart page")
    public void i_open_the_cart_page() {
        new InventoryPage().openCart();
        Assert.assertTrue("Cart page did not load", new CartPage().isLoaded());
    }

    @And("I proceed to checkout information page")
    public void i_proceed_to_checkout_information_page() {
        CartPage cart = new CartPage();
        cart.proceedToCheckout();
        Assert.assertTrue("Checkout Info page did not load", new CheckoutInfoPage().isLoaded());
    }

    @And("I fill the checkout form with valid data")
    public void i_fill_the_checkout_form_with_valid_data() {
        CheckoutInfoPage info = new CheckoutInfoPage();
        info.fillInfo("John", "Doe", "12345").continueToOverview();
        Assert.assertTrue("Checkout Overview did not load", new CheckoutOverviewPage().isLoaded());
    }


    @And("I try to login with invalid credentials")
    public void i_try_to_login_with_invalid_credentials() {
        new LoginPage().open().enterUsername("invalid_user").enterPassword("wrong_pass").submitLogin();
        // Intentionally no assertion; we just want the error state rendered for scanning
    }

    @When("I scan the page for accessibility issues")
    public void i_scan_the_page_for_accessibility_issues() {
        // Use common WCAG tags; can be tuned as needed
        AxeBuilder builder = new AxeBuilder()
                .withTags(List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "best-practice"));
        lastResults = builder.analyze(driver);

        // Persist raw JSON for later analysis
        try {
            Path outDir = Path.of("target", "accessibility");
            Files.createDirectories(outDir);
            String json = new Gson().toJson(lastResults);
            Path out = outDir.resolve("axe-results-" + System.currentTimeMillis() + ".json");
            Files.writeString(out, json);
            Hooks.getScenario().attach(json, "application/json", "axe-results.json");
        } catch (Exception ignored) {}
    }

    @Then("there should be no serious or critical accessibility violations")
    public void there_should_be_no_serious_or_critical_accessibility_violations() {
        Assert.assertNotNull("No axe results captured. Did you run the scan step?", lastResults);
        List<Rule> violatingRules = lastResults.getViolations();
        List<Rule> seriousOrCritical = violatingRules == null ? List.of() : violatingRules.stream()
                .filter(r -> {
                    String impact = r.getImpact();
                    return impact != null && List.of("serious", "critical").contains(impact.toLowerCase(Locale.ROOT));
                })
                .toList();

        if (!seriousOrCritical.isEmpty()) {
            String summary = seriousOrCritical.stream()
                    .map(r -> "- [" + r.getImpact() + "] " + r.getId() + ": " + Objects.toString(r.getDescription(), ""))
                    .collect(Collectors.joining("\n"));
            Hooks.getScenario().attach(summary, "text/plain", "axe-violations.txt");
        }

        Assert.assertTrue(
                "Found serious/critical accessibility violations:\n" +
                        (lastResults.getUrl() != null ? ("URL: " + lastResults.getUrl() + "\n") : "") +
                        seriousOrCritical.stream().map(r -> r.getId() + " (" + r.getImpact() + ")").collect(Collectors.joining(", ")),
                seriousOrCritical.isEmpty());
    }
}
