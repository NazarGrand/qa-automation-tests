package com.example.qa.ui.pages;

import com.codeborne.selenide.Selenide;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage {
    public LoginPage open() {
        Selenide.open("/");
        $("[data-test='username']").shouldBe(visible);
        return this;
    }

    public ProductsPage loginAs(String username, String password) {
        $("[data-test='username']").setValue(username);
        $("[data-test='password']").setValue(password);
        $("[data-test='login-button']").click();
        return new ProductsPage().shouldBeOpen();
    }
}
