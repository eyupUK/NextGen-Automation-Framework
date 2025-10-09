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

        // Collect args so we can log them without calling non-existent getters
        java.util.List<String> args = new java.util.ArrayList<>();

        if (headless) {
            args.add("--headless=new");
        }

        // CI-friendly flags for Linux runners
        args.add("--no-sandbox");
        args.add("--disable-dev-shm-usage");
        args.add("--disable-gpu");
        args.add("--disable-notifications");
        args.add("--disable-save-password-bubble");
        args.add("--incognito");

        // Unique profile dir per session to avoid "user data directory is already in use"
        String baseUserDataDir = System.getProperty("chrome.userDataDir",
                System.getProperty("java.io.tmpdir"));
        java.nio.file.Path uniqueProfile =
                java.nio.file.Path.of(baseUserDataDir, "chrome-profile-" + java.util.UUID.randomUUID());
        try {
            java.nio.file.Files.createDirectories(uniqueProfile);
        } catch (Exception ignored) {}
        args.add("--user-data-dir=" + uniqueProfile.toAbsolutePath());

        // Apply all args at once
        options.addArguments(args);

        // Disable password manager UI
        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        // Optional: override Chrome binary via -Dchrome.binary=/path/to/chrome
        String chromeBinary = System.getProperty("chrome.binary");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }

        // ---- Log for CI verification ----
        System.out.println("[Driver] Browser=chrome, headless=" + headless);
        System.out.println("[Driver] user-data-dir=" + uniqueProfile.toAbsolutePath());
        System.out.println("[Driver] Chrome args=" + args);

        return options;
    }

}
