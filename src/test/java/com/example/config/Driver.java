package com.example.config;

import com.example.util.ConfigurationReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Driver {

    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();

    public static WebDriver get() {
        if (TL_DRIVER.get() == null) {
            synchronized (Driver.class) {
                if (TL_DRIVER.get() == null) {
                    final String configured = System.getProperty("browser", ConfigurationReader.get("browser"));
                    final String browser = configured == null ? "chrome" : configured.trim().toLowerCase();

                    switch (browser) {
                        case "chrome":
                        case "chrome-headless":
                        case "chrome-local":
                            TL_DRIVER.set(new ChromeDriver(buildChromeOptions(resolveHeadlessDefault(browser))));
                            break;

                        case "firefox": {
                            FirefoxOptions ff = new FirefoxOptions();
                            ff.setAcceptInsecureCerts(true);
                            TL_DRIVER.set(new FirefoxDriver(ff));
                            break;
                        }
                        case "firefox-headless": {
                            FirefoxOptions ff = new FirefoxOptions();
                            ff.addArguments("-headless");
                            ff.setAcceptInsecureCerts(true);
                            TL_DRIVER.set(new FirefoxDriver(ff));
                            break;
                        }
                        case "edge": {
                            if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                                throw new WebDriverException("Your OS doesn't support Edge");
                            }
                            EdgeOptions edge = new EdgeOptions();
                            TL_DRIVER.set(new EdgeDriver(edge));
                            break;
                        }
                        case "safari": {
                            if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
                                throw new WebDriverException("Your OS doesn't support Safari");
                            }
                            SafariOptions safari = new SafariOptions();
                            TL_DRIVER.set(new SafariDriver(safari));
                            break;
                        }
                        default:
                            // Default to Chrome with CI-safe options
                            TL_DRIVER.set(new ChromeDriver(buildChromeOptions(resolveHeadlessDefault("chrome"))));
                    }
                }
            }
        }
        return TL_DRIVER.get();
    }

    public static void closeDriver() {
        WebDriver driver = TL_DRIVER.get();
        if (driver != null) {
            try { driver.quit(); } finally { TL_DRIVER.remove(); }
        }
    }

    private static boolean resolveHeadlessDefault(String browser) {
        // If explicitly provided via -Dheadless, honor it.
        String prop = System.getProperty("headless");
        if (prop != null && !prop.isBlank()) {
            return Boolean.parseBoolean(prop);
        }
        // Otherwise: headless on CI (env CI=true), headful locally.
        boolean onCI = "true".equalsIgnoreCase(System.getenv("CI"));
        if ("chrome-headless".equals(browser)) return true;
        return onCI;
    }

    private static ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }

        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-notifications",
                "--disable-save-password-bubble",
                "--incognito"
        );

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        // Base directory (from CI step), default to system temp.
        String baseUserDataDir = System.getProperty("chrome.userDataDir", System.getProperty("java.io.tmpdir"));
        Path uniqueProfile = Path.of(baseUserDataDir, "chrome-profile-" + UUID.randomUUID());
        try { Files.createDirectories(uniqueProfile); } catch (Exception ignored) {}
        options.addArguments("--user-data-dir=" + uniqueProfile.toAbsolutePath());

        String chromeBinary = System.getProperty("chrome.binary");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }

        // ----- LOG so we can verify in CI logs -----
        System.out.println("[Driver] Browser=chrome, headless=" + headless);
        System.out.println("[Driver] user-data-dir=" + uniqueProfile.toAbsolutePath());
        System.out.println("[Driver] Chrome args=" + options.getExperimentalOption("args"));

        return options;
    }
}
