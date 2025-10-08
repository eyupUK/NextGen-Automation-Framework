package com.example.steps.ui;

import com.example.pages.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class CheckoutSteps {

    LoginPage login = new LoginPage();
    InventoryPage inventory = new InventoryPage();
    CartPage cart = new CartPage();
    CheckoutInfoPage info = new CheckoutInfoPage();
    CheckoutOverviewPage overview = new CheckoutOverviewPage();
    CheckoutCompletePage complete = new CheckoutCompletePage();



    @When("I add {string} to my cart and start checkout")
    public void i_add_item_and_start_checkout(String productName) {
        inventory.addProductToCartByName(productName);
        Assert.assertEquals( "Cart badge mismatch", 1, inventory.getCartBadgeCount().intValue());
        inventory.openCart();
        Assert.assertTrue("Cart page not loaded.", cart.isLoaded());
        cart.proceedToCheckout();
        Assert.assertTrue("Checkout info page not loaded.", info.isLoaded());
    }

    @When("I provide checkout details {string} , {string} , {string} and continue")
    public void i_provide_checkout_details_and_continue(String first, String last, String zip) {
        info.fillInfo(first, last, zip).continueToOverview();
        Assert.assertTrue("Overview page not loaded.", overview.isLoaded());
    }

    @When("I finish the checkout")
    public void i_finish_the_checkout() {
        overview.finish();
    }

    @Then("I should see the order completion page")
    public void i_should_see_completion_page() {
        Assert.assertTrue("Order completion page not shown.", complete.isLoaded());
    }
}
