package com.brandon.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public final class ConfigManager {

    private static final String ENVIRONMENT = resolveEnvironment();
    private static final Properties SETTINGS = loadSettings();

    private ConfigManager() {
        // Utility class: prevent instantiation.
    }

    public static String getEnvironment() {
        return ENVIRONMENT;
    }

    public static String getBaseUrl() {
        return validateBaseUrl(requiredValue("baseUrl", "API_BASE_URL", "base.url"));
    }

    public static String getUsername() {
        return requiredValue("apiUsername", "API_USERNAME", "auth.username");
    }

    public static String getPassword() {
        return requiredValue("apiPassword", "API_PASSWORD", "auth.password");
    }

    public static String validateBaseUrl(String value) {
        if (!hasText(value)) {
            throw new IllegalArgumentException("Base URL must not be blank");
        }

        URI uri;
        try {
            uri = URI.create(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Base URL must be a valid HTTP or HTTPS URL");
        }

        boolean validScheme = "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());

        if (!validScheme || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException(
                    "Base URL must have an HTTP(S) scheme and host, "
                    + "without credentials, a query, or a fragment"
            );
        }

        return uri.toString();
    }

    private static String resolveEnvironment() {
        String value = firstNonBlank(System.getProperty("env"), System.getenv("TEST_ENV"), "dummyjson");
        String environment = value.trim().toLowerCase(Locale.ROOT);

        if (!environment.matches("[a-z0-9-]+")) {
            throw new IllegalArgumentException(
                    "Environment name may contain only letters, numbers, and hyphens"
            );
        }

        return environment;
    }

    private static Properties loadSettings() {
        String resource = "environments/" + ENVIRONMENT + ".properties";

        try (InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Environment file not found: " + resource);
            }

            Properties properties = new Properties();
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load environment configuration", exception);
        }
    }

    private static String requiredValue(String systemProperty, String environmentVariable, String fileKey) {
        String value = firstNonBlank(
                System.getProperty(systemProperty),
                System.getenv(environmentVariable),
                SETTINGS.getProperty(fileKey)
        );

        if (value == null) {
            throw new IllegalStateException(
                    "Missing configuration: " + fileKey + " for environment: " + ENVIRONMENT
            );
        }

        return value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
