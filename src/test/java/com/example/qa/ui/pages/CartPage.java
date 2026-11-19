package com.example.qa.ui.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;

public class CartPage {
    public CartPage shouldBeOpen() {
        webdriver().shouldHave(url(com.codeborne.selenide.Configuration.baseUrl + "/cart.html"));
        $("[data-test='title']").shouldHave(exactText("Your Cart"));
        return this;
    }

    public void shouldContainOnly(Product expected) {
        $$("[data-test='inventory-item']").shouldHave(size(1));
        SelenideElement item = $("[data-test='inventory-item']").shouldBe(visible);
        item.$("[data-test='inventory-item-name']").shouldHave(exactText(expected.name()));
        item.$("[data-test='inventory-item-price']").shouldHave(exactText(expected.price()));
        item.$("[data-test='item-quantity']").shouldHave(exactText("1"));
    }
}
