package com.brandon.qa.specification;

import com.brandon.qa.config.ConfigManager;

import io.restassured.http.ContentType;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecFactory {

    private RequestSpecFactory() {
        // Utility class: prevent instantiation.
    }

    public static RequestSpecification create() {
        return create(ConfigManager.getBaseUrl());
    }

    public static RequestSpecification create(String baseUrl) {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.validateBaseUrl(baseUrl))
                .setAccept(ContentType.JSON)
                .build();
    }
}
