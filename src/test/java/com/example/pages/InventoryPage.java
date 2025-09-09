package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryPage extends BasePage {private final By title = By.cssSelector(".title");
    private final By cartBadge = By.cssSelector(".shopping_cart_badge");
    private final By cartLink = By.id("shopping_cart_container");
    private final By inventoryItem = By.cssSelector(".inventory_item");
    private final By sortSelect = By.cssSelector("[data-test='product-sort-container']");
    private final By priceLabel = By.cssSelector(".inventory_item_price");

    public boolean isLoaded() { return text(title).equals("Products"); }

    public void addProductToCartByName(String productName) {
        List<WebElement> items = driver.findElements(inventoryItem);
        WebElement target = items.stream()
                .filter(it -> it.findElement(By.className("inventory_item_name"))
                        .getText().equals(productName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found: " + productName));
        target.findElement(By.tagName("button")).click();
    }

    public Integer getCartBadgeCount() {
        if (!isPresent(cartBadge)) return 0;
        return Integer.parseInt(text(cartBadge).trim());
    }

    public void openCart() { click(cartLink); }

    public List<String> getAllProductNames() {
        return driver.findElements(inventoryItem).stream()
                .map(it -> it.findElement(By.className("inventory_item_name")).getText())
                .collect(Collectors.toList());
    }

    public void sortByVisibleText(String visibleText) {
        new Select(waitVisible(sortSelect)).selectByVisibleText(visibleText);
    }

    public List<Double> getAllPricesInOrder() {
        return driver.findElements(priceLabel).stream()
                .map(WebElement::getText)          // e.g. "$29.99"
                .map(p -> p.replaceAll("[^0-9.]", ""))
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }
}
