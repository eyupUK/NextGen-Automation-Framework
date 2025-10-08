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

/**
 * Singleton class to get and manage WebDriver instances for different browsers.
 *
 * @author [eyupUK]
 * @version 1.1
 */
public class Driver {

    /**
     * Thread-confined WebDriver instance.
     */
    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();

    /**
     * @return the WebDriver instance for the specified browser.
     */
    public static WebDriver get() {
        if (TL_DRIVER.get() == null) {
            synchronized (Driver.class) {
                if (TL_DRIVER.get() == null) {
                    String browser = System.getProperty("browser",
                            ConfigurationReader.get("browser"));

                    switch (browser == null ? "" : browser.trim().toLowerCase()) {
                        case "chrome-local" -> TL_DRIVER.set(new ChromeDriver(buildChromeOptions(false)));
                        case "chrome-headless" -> TL_DRIVER.set(new ChromeDriver(buildChromeOptions(true)));
                        case "chrome" -> TL_DRIVER.set(new ChromeDriver(buildChromeOptions(
                                Boolean.parseBoolean(System.getProperty("headless", "false"))
                        )));
                        case "firefox" -> {
                            FirefoxOptions ff = new FirefoxOptions();
                            ff.setAcceptInsecureCerts(true);
                            TL_DRIVER.set(new FirefoxDriver(ff));
                        }
                        case "firefox-headless" -> {
                            FirefoxOptions ff = new FirefoxOptions();
                            ff.addArguments("-headless");
                            ff.setAcceptInsecureCerts(true);
                            TL_DRIVER.set(new FirefoxDriver(ff));
                        }
                        case "edge" -> {
                            if (!System.getProperty("os.name").toLowerCase().contains("windows"))
                                throw new WebDriverException("Your OS doesn't support Edge");
                            EdgeOptions edge = new EdgeOptions();
                            TL_DRIVER.set(new EdgeDriver(edge));
                        }
                        case "safari" -> {
                            if (!System.getProperty("os.name").toLowerCase().contains("mac"))
                                throw new WebDriverException("Your OS doesn't support Safari");
                            SafariOptions safari = new SafariOptions();
                            TL_DRIVER.set(new SafariDriver(safari));
                        }
                        default -> {
                            // Sensible default: Chrome headless on CI
                            TL_DRIVER.set(new ChromeDriver(buildChromeOptions(
                                    Boolean.parseBoolean(System.getProperty("headless", "true"))
                            )));
                        }
                    }
                }
            }
        }
        return TL_DRIVER.get();
    }

    /**
     * Close and remove the thread's WebDriver instance.
     */
    public static void closeDriver() {
        WebDriver driver = TL_DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                TL_DRIVER.remove();
            }
        }
    }

    /**
     * Build CI-safe ChromeOptions with a unique user-data-dir per session.
     * Headless defaults to true (override via -Dheadless=false locally).
     */
    private static ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        // Headless mode
        if (headless) {
            options.addArguments("--headless=new");
        }

        // CI-friendly flags for Linux runners
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-notifications",
                "--disable-save-password-bubble",
                "--incognito"
        );

        // Preferences: disable password manager bubbles
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        // Unique profile dir per session to avoid "user data directory is already in use"
        String baseUserDataDir = System.getProperty("chrome.userDataDir",
                System.getProperty("java.io.tmpdir"));
        Path uniqueProfile = Path.of(baseUserDataDir, "chrome-profile-" + UUID.randomUUID());
        try {
            Files.createDirectories(uniqueProfile);
        } catch (Exception ignored) { }
        options.addArguments("--user-data-dir=" + uniqueProfile.toAbsolutePath());

        // Optional: if you explicitly provide a Chrome binary path: -Dchrome.binary=/path/to/chrome
        String chromeBinary = System.getProperty("chrome.binary");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }

        // Let Selenium Manager resolve the proper driver by default.
        // If you insist on a local driver, set it externally via -Dwebdriver.chrome.driver=...
        return options;
    }
}
