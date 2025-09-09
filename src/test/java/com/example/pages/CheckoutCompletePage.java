package com.example.pages;

import org.openqa.selenium.By;

public class CheckoutCompletePage extends BasePage {
    private final By completeHeader = By.cssSelector(".complete-header");
    private final By backHome = By.id("back-to-products");

    public boolean isLoaded() {
        return text(completeHeader).equals("Thank you for your order!");
    }

    public void backHome() { click(backHome); }
}
