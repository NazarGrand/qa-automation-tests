package com.example.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public final class TestConfig {
    private static final Properties DEFAULTS = loadDefaults();

    private TestConfig() {}

    public static String get(String key) {
        String value = System.getProperty(key, DEFAULTS.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing configuration: " + key);
        }
        return value;
    }

    public static int positiveInt(String key) {
        int value = Integer.parseInt(get(key));
        if (value <= 0) {
            throw new IllegalArgumentException(key + " must be positive");
        }
        return value;
    }

    private static Properties loadDefaults() {
        Properties properties = new Properties();
        try (InputStream stream = TestConfig.class.getResourceAsStream("/test.properties")) {
            if (stream == null) {
                throw new IllegalStateException("test.properties is missing");
            }
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read test configuration", e);
        }
    }
}
