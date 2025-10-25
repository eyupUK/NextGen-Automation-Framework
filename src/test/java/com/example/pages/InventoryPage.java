package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InventoryPage extends BasePage {private final By title = By.cssSelector(".title");
    private final By cartBadge = By.cssSelector(".shopping_cart_badge");
    private final By cartLink = By.id("shopping_cart_container");
    private final By inventoryItem = By.cssSelector(".inventory_item");
    private final By sortSelect = By.cssSelector("[data-test='product-sort-container']");
    private final By priceLabel = By.cssSelector(".inventory_item_price");
    private final By addToCart = By.cssSelector("button[data-test^=add-to-cart]");
    private final By productTitles = By.cssSelector("[data-test=inventory-item-name]");


    public boolean isLoaded() { return text(title).equals("Products"); }

    public void addProductToCartByName(String productName) {
        List<WebElement> items = driver.findElements(inventoryItem);
        WebElement target = items.stream()
                .filter(it -> it.findElement(productTitles)
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
        return texts(productTitles);
    }

    public void sortByVisibleText(String visibleText) {
        new Select(waitVisible(sortSelect)).selectByVisibleText(visibleText);
    }

    public List<Double> getAllPricesInOrder() {
        return texts(priceLabel).stream()// e.g. "$29.99"
                .map(p -> p.replaceAll("[^0-9.]", ""))
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    public Integer findHighestPriceIndex(List<Double> prices){
        int indexHighest = 0;
        double highest = prices.get(0);
        for (int i = 0; i < prices.size(); i++){
            if(highest < prices.get(i)){
                highest = prices.get(i);
                indexHighest = i;
            }
        }
        return indexHighest;
    }

    public void addHighestPriceItemToCart(){
        int highestIndex = findHighestPriceIndex(getAllPricesInOrder());
        driver.findElements(addToCart).get(highestIndex).click();
    }
    public Double selectHighestPrice(){
        int highestIndex = findHighestPriceIndex(getAllPricesInOrder());
        Double highestPrice = getAllPricesInOrder().get(highestIndex);
        driver.findElements(productTitles).get(highestIndex).click();
        return highestPrice;
    }


    public List<Double> getAllPricesInOrderByLoop() {
        List<Double> prices = new ArrayList<>();
        List<String> strings = texts(priceLabel);
        for (String price: strings) {
            prices.add(Double.parseDouble(price.replaceAll("[^0-9.]", "")));
        }
        return prices;
    }

}
