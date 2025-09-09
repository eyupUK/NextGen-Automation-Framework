package com.example.pages;

import org.openqa.selenium.By;

public class CheckoutOverviewPage extends BasePage {
    private final By title = By.cssSelector(".title");
    private final By finishBtn = By.id("finish");

    public boolean isLoaded() { return text(title).equals("Checkout: Overview"); }

    public void finish() { click(finishBtn); }
}
