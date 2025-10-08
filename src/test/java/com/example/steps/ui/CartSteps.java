package com.example.steps.ui;

import com.example.pages.CartPage;
import com.example.pages.InventoryPage;
import com.example.pages.LoginPage;
import com.example.util.ConfigurationReader;
import io.cucumber.java.en.*;
import org.junit.Assert;


import java.util.List;

public class CartSteps {

    LoginPage login = new LoginPage();
    InventoryPage inventory = new InventoryPage();
    CartPage cart = new CartPage();
    String username = ConfigurationReader.get("STANDARD_USER");
    String password = ConfigurationReader.get("STANDARD_PASSWORD");

    @Given("I am logged in as a standard user")
    public void i_am_logged_in_as_standard_user() {
        login.loginAsAStandartUser(username, password);
        Assert.assertTrue("Login failed; products page not loaded.", inventory.isLoaded());
    }

    @When("I add the product {string} to the cart")
    public void i_add_product_to_cart(String productName) {
        inventory.addProductToCartByName(productName);
    }

    @Then("the cart badge should show {int}")
    public void the_cart_badge_should_show(int expectedCount) {
        Assert.assertEquals("Cart badge count mismatch", inventory.getCartBadgeCount().intValue(), expectedCount);
    }

    @When("I open the cart")
    public void i_open_the_cart() {
        inventory.openCart();
        Assert.assertTrue("Cart page did not load.", cart.isLoaded());
    }

    @Then("the cart should contain {string}")
    public void the_cart_should_contain(String expectedProduct) {
        List<String> names = cart.itemNames();
        Assert.assertTrue("Cart does not contain expected product: " + expectedProduct + " | Actual: " + names,
                names.contains(expectedProduct));
    }
}
