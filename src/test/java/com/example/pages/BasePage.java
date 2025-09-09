package com.example.pages;

import com.example.config.Driver;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {
    protected WebDriver driver;
    private WebDriverWait wait;

    public BasePage() {
        this.driver = Driver.get();
        // Tight, explicit wait to handle sync reliably
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitClickable(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement e = waitVisible(locator);
        e.clear();
        e.sendKeys(text);
    }

    protected String text(By locator) {
        return waitVisible(locator).getText();
    }

    protected boolean isPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    protected void goTo(String url) {
        driver.get(url);
    }

}
