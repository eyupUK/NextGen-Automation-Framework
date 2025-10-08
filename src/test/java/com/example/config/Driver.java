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

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

/**
 * Singleton class to get and manage WebDriver instances for different browsers.
 *
 * @author [eyupUK]
 * @version 1.0
 */
public class Driver {


    /**
     * Singleton instance of the WebDriver.
     */
    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();

    /**
     * @return the WebDriver instance for the specified browser.
     */
    public static WebDriver get() {

        if (TL_DRIVER.get() == null) {
            // Get the browser type from the system properties or configuration file
            String browser = System.getProperty("browser") != null ? System.getProperty("browser") : ConfigurationReader.get("browser");
            synchronized (Driver.class) {
                switch (browser) {
                    case "chrome-local" -> {
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--disable-gpu");
                        options.addArguments("--disable-save-password-bubble");
                        options.addArguments("--disable-notifications");
                        options.addArguments("--incognito");

                        // Create unique user data directory with proper permissions
                        String userDataDir = System.getProperty("java.io.tmpdir") + File.separator + "chrome-test-" + UUID.randomUUID();
                        File userDir = new File(userDataDir);
                        if (!userDir.exists()) {
                            userDir.mkdirs();
                        }
                        options.addArguments("--user-data-dir=" + userDataDir);
                        options.addArguments("--remote-allow-origins=*");
                        options.addArguments("--no-sandbox");
                        options.addArguments("--disable-dev-shm-usage");

                        options.setExperimentalOption("prefs", new HashMap<String, Object>() {{
                            put("credentials_enable_service", false);
                            put("profile.password_manager_enabled", false);
                        }});
                        // Install chromedriver first and move it to the directory below to set the path to the chromedriver executable
                        System.setProperty("webdriver.chrome.driver", "browserdriver/chromedriver");
                        TL_DRIVER.set(new ChromeDriver(options));
                    }
                    case "chrome" -> {
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--disable-gpu");
                        options.addArguments("--disable-save-password-bubble");
                        options.addArguments("--disable-notifications");
                        options.addArguments("--incognito");

                        // Create unique user data directory with proper permissions
                        String userDataDir = System.getProperty("java.io.tmpdir") + File.separator + "chrome-test-" + UUID.randomUUID();
                        File userDir = new File(userDataDir);
                        if (!userDir.exists()) {
                            userDir.mkdirs();
                        }
                        options.addArguments("--user-data-dir=" + userDataDir);
                        options.addArguments("--remote-allow-origins=*");

                        // Additional CI/CD stability arguments
                        options.addArguments("--no-sandbox");
                        options.addArguments("--disable-dev-shm-usage");
                        options.addArguments("--disable-extensions");

                        options.setExperimentalOption("prefs", new HashMap<String, Object>() {{
                            put("credentials_enable_service", false);
                            put("profile.password_manager_enabled", false);
                        }});
                        TL_DRIVER.set(new ChromeDriver(options));
                    }
                    case "chrome-headless" -> {
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--headless=new");
                        options.addArguments("--disable-gpu");
                        options.addArguments("--disable-save-password-bubble");
                        options.addArguments("--disable-notifications");
                        options.addArguments("--incognito");

                        // Create unique user data directory with proper permissions
                        String userDataDir = System.getProperty("java.io.tmpdir") + File.separator + "chrome-test-" + UUID.randomUUID();
                        File userDir = new File(userDataDir);
                        if (!userDir.exists()) {
                            userDir.mkdirs();
                        }
                        options.addArguments("--user-data-dir=" + userDataDir);
                        options.addArguments("--remote-allow-origins=*");
                        options.addArguments("--no-sandbox");
                        options.addArguments("--disable-dev-shm-usage");
                        options.addArguments("--disable-extensions");
                        options.addArguments("--disable-infobars");

                        options.setExperimentalOption("prefs", new HashMap<String, Object>() {{
                            put("credentials_enable_service", false);
                            put("profile.password_manager_enabled", false);
                        }});
                        TL_DRIVER.set(new ChromeDriver(options));
                    }
                    case "firefox" -> {
                        FirefoxOptions options = new FirefoxOptions();
                        options.setAcceptInsecureCerts(true);
                        TL_DRIVER.set(new FirefoxDriver(options));
                    }
                    case "firefox-headless" -> {
                        FirefoxOptions options = new FirefoxOptions();
                        options.addArguments("-headless");
                        TL_DRIVER.set(new FirefoxDriver(options));
                    }
                    case "edge" -> {
                        if (!System.getProperty("os.name").toLowerCase().contains("windows"))
                            throw new WebDriverException("Your OS doesn't support Edge");
                        EdgeOptions options = new EdgeOptions();
                        TL_DRIVER.set(new EdgeDriver(options));
                    }
                    case "safari" -> {
                        if (!System.getProperty("os.name").toLowerCase().contains("mac"))
                            throw new WebDriverException("Your OS doesn't support Safari");
                        SafariOptions options = new SafariOptions();
                        TL_DRIVER.set(new SafariDriver(options));
                    }
                }
            }
        }
        return TL_DRIVER.get();
    }

    /**
     * Close the WebDriver instance.
     */
    public static void closeDriver() {
        WebDriver driver = TL_DRIVER.get();
        if (driver != null) {
            driver.quit();
            TL_DRIVER.remove();
        }
    }
}