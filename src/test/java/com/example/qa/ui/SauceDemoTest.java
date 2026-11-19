package com.example.qa.ui;

import com.example.qa.config.TestConfig;
import com.example.qa.ui.pages.LoginPage;
import com.example.qa.ui.pages.Product;
import com.example.qa.ui.pages.ProductsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Tag("ui")
class SauceDemoTest extends BaseUiTest {
    @Test
    @DisplayName("Standard user can log in and see products")
    void loginShowsProducts() {
        login().shouldContainProducts();
    }

    @Test
    @DisplayName("Products sort by ascending price and the selected product appears in the cart")
    void sortedProductCanBeAddedToCart() {
        ProductsPage products = login().shouldContainProducts();
        List<String> expectedPrices = products.prices().stream()
                .sorted(Comparator.comparing(price -> new BigDecimal(price.replace("$", ""))))
                .toList();
        products.sortByPriceLowToHigh().shouldHavePrices(expectedPrices);

        Product selected = products.addFirstProductToCart();
        products.openCart().shouldContainOnly(selected);
    }

    private ProductsPage login() {
        return new LoginPage().open().loginAs(
                TestConfig.get("ui.username"), TestConfig.get("ui.password"));
    }
}
