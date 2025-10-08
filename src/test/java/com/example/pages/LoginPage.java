package com.example.pages;

import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    private final By username = By.id("user-name");
    private final By password = By.id("password");
    private final By loginBtn = By.id("login-button");
    private final By errorMsg = By.cssSelector("h3[data-test='error']");
    private final By accountBtn = By.id("user");


    public LoginPage open() {
        goTo("https://www.saucedemo.com/");
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

    public void loginAsAStandartUser(String user, String pass) {
        enterUsername(user);
        enterPassword(pass);
        submitLogin();
    }

    public void logout() {

    }
}
