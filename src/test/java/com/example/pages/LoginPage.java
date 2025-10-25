package com.example.pages;

import org.openqa.selenium.By;
import com.example.util.ConfigurationReader;

public class LoginPage extends BasePage {

    private final By username = By.id("user-name");
    private final By password = By.id("password");
    private final By loginBtn = By.id("login-button");
    private final By errorMsg = By.cssSelector("h3[data-test='error']");
    private final By accountBtn = By.id("user");


    public LoginPage open() {
        String base = ConfigurationReader.get("sauceDemoUrl");
        if (base == null || base.isBlank()) {
            base = "https://www.saucedemo.com/";
        }
        goTo(base);
        return this;
    }

    public LoginPage enterUsername(String user) {
        type(username, user);
        return this;
    }

    public LoginPage enterPassword(String pass) {
        type(password, pass);
        return this;
    }

    public void submitLogin() {
        click(loginBtn);
    }

    public String getError() {
        return text(errorMsg);
    }

    public void loginAsAStandartUser(String user, String pwd) {
        enterUsername(user);
        enterPassword(pwd);
        submitLogin();
    }

    public void logout() {

    }
}
