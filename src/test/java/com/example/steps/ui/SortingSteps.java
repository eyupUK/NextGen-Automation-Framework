package com.example.steps.ui;

import com.example.pages.InventoryPage;
import io.cucumber.java.en.*;
import org.junit.Assert;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortingSteps {

    InventoryPage inventory = new InventoryPage();

    @When("I sort products by {string}")
    public void i_sort_products_by(String visibleText) {
        Assert.assertTrue("Products page must be loaded", inventory.isLoaded());
        inventory.sortByVisibleText(visibleText);
    }

    @Then("product prices should be in ascending order")
    public void product_prices_should_be_in_ascending_order() {
        List<Double> prices = inventory.getAllPricesInOrder();
        List<Double> sorted = new ArrayList<>(prices);
        Collections.sort(sorted);
        Assert.assertEquals("Prices are not ascending: " + prices, prices, sorted);
    }

    @Then("product names should be in descending order")
    public void product_names_should_be_in_descending_order() {
        List<String> names = inventory.getAllProductNames();
        List<String> sorted = new ArrayList<>(names);
        sorted.sort(String.CASE_INSENSITIVE_ORDER.reversed());
        Assert.assertEquals("Names are not Z→A: " + names, names, sorted);
    }
}

