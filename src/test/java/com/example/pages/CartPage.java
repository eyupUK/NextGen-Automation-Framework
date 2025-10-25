package com.example.pages;

import org.openqa.selenium.By;

import java.util.List;
import java.util.stream.Collectors;

public class CartPage extends BasePage {
    private final By cartTitle = By.cssSelector(".title");
    private final By cartItems = By.cssSelector(".cart_item");
    private final By itemName = By.cssSelector(".inventory_item_name");
    private final By checkoutBtn = By.id("checkout");

    public boolean isLoaded() { return text(cartTitle).equals("Your Cart"); }

    public List<String> itemNames() {
        return texts(itemName);
    }

    public void proceedToCheckout() { click(checkoutBtn); }
}
