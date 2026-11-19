package com.example.qa.ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.List;

import static com.codeborne.selenide.CollectionCondition.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;

public class ProductsPage {
    private final ElementsCollection products = $$("[data-test='inventory-item']");
    private final ElementsCollection prices = $$("[data-test='inventory-item-price']");

    public ProductsPage shouldBeOpen() {
        webdriver().shouldHave(url(com.codeborne.selenide.Configuration.baseUrl + "/inventory.html"));
        $("[data-test='title']").shouldHave(exactText("Products"));
        $("[data-test='inventory-list']").shouldBe(visible);
        return this;
    }

    public ProductsPage shouldContainProducts() {
        products.shouldHave(sizeGreaterThan(0));
        products.first().shouldBe(visible);
        return this;
    }

    public List<String> prices() {
        prices.shouldHave(sizeGreaterThan(1));
        return prices.texts();
    }

    public ProductsPage sortByPriceLowToHigh() {
        $("[data-test='product-sort-container']").selectOptionByValue("lohi");
        $("[data-test='product-sort-container']").shouldHave(value("lohi"));
        return this;
    }

    public ProductsPage shouldHavePrices(List<String> expectedPrices) {
        prices.shouldHave(exactTexts(expectedPrices));
        return this;
    }

    public Product addFirstProductToCart() {
        SelenideElement item = products.first().shouldBe(visible);
        Product selected = new Product(
                item.$("[data-test='inventory-item-name']").getText(),
                item.$("[data-test='inventory-item-price']").getText());
        item.$("button[data-test^='add-to-cart-']").click();
        item.$("button[data-test^='remove-']").shouldBe(visible);
        $("[data-test='shopping-cart-badge']").shouldHave(exactText("1"));
        return selected;
    }

    public CartPage openCart() {
        $("[data-test='shopping-cart-link']").click();
        return new CartPage().shouldBeOpen();
    }
}
