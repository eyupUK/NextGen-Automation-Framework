package com.example.pages;

import org.openqa.selenium.By;

public class CheckoutInfoPage extends BasePage {
    private final By title = By.cssSelector(".title");
    private final By firstName = By.id("first-name");
    private final By lastName = By.id("last-name");
    private final By postalCode = By.id("postal-code");
    private final By continueBtn = By.id("continue");

    public boolean isLoaded() { return text(title).equals("Checkout: Your Information"); }

    public CheckoutInfoPage fillInfo(String f, String l, String z) {
        type(firstName, f);
        type(lastName, l);
        type(postalCode, z);
        return this;
    }

    public void continueToOverview() { click(continueBtn); }
}
