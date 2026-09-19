package com.brandon.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigManager {

    private static final String CONFIG_FILE = "config.properties";

    private static final Properties PROPERTIES = loadProperties();

    private ConfigManager() {
        // Utility class: prevent instantiation.
    }

    private static Properties loadProperties() {

        Properties properties = new Properties();

        try (InputStream input =
                     ConfigManager.class
                             .getClassLoader()
                             .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Configuration file not found: " + CONFIG_FILE
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load configuration file: " + CONFIG_FILE,
                    e
            );
        }

        return properties;
    }

    public static String getBaseUrl() {

        String baseUrl = System.getProperty("baseUrl");

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = PROPERTIES.getProperty("base.url");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "API base URL is not configured."
            );
        }

        return baseUrl.trim();
    }
}