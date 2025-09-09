package com.example.steps.ui;

import com.example.pages.InventoryPage;
import com.example.pages.LoginPage;
import io.cucumber.java.en.*;
import org.junit.Assert;


public class LoginSteps {

    LoginPage login = new LoginPage();
    InventoryPage inventory = new InventoryPage();

    @Given("I am on the SauceDemo login page")
    public void i_am_on_login_page() {
        login.open();
    }

    @When("I login with username {string} and password {string}")
    public void i_login_with_credentials(String user, String pass) {
        login.enterUsername(user).enterPassword(pass).submitLogin();
    }

    @Then("I should see the products page")
    public void i_should_see_products_page() {
        Assert.assertTrue("Products page did not load.", inventory.isLoaded());
    }

    @Then("I should see an error message containing {string}")
    public void i_should_see_error(String expected) {
        String actual = login.getError();
        Assert.assertTrue("Expected error to contain: " + expected + " but was: " + actual,
                actual.contains(expected));
    }
}
