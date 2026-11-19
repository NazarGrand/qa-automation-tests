package com.example.qa.ui;

import com.codeborne.selenide.Configuration;
import com.example.qa.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.closeWebDriver;

abstract class BaseUiTest {
    @BeforeEach
    void configureBrowser() {
        Configuration.baseUrl = TestConfig.get("ui.baseUrl");
        Configuration.browser = "chrome";
        Configuration.headless = Boolean.parseBoolean(TestConfig.get("ui.headless"));
        Configuration.timeout = TestConfig.positiveInt("ui.timeoutMs");
        Configuration.pageLoadTimeout = 30000;
        Configuration.browserSize = "1440x1000";
        Configuration.reportsFolder = "target/selenide-reports";
        Configuration.downloadsFolder = "target/downloads";
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getenv("CHROME_NO_SANDBOX"))) {
            options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        }
        String binary = System.getenv("CHROME_BIN");
        if (binary != null && !binary.isBlank()) {
            options.setBinary(binary);
        }
        Configuration.browserCapabilities = options;
    }

    @AfterEach
    void closeBrowser() {
        closeWebDriver();
    }
}
