package com.brandon.qa.client;

import com.brandon.qa.specification.RequestSpecFactory;
import com.brandon.qa.config.ConfigManager;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.RestAssuredConfig.config;

public class ProductClient {

    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 10_000;

    private final String baseUrl;
    private final int readTimeoutMillis;

    public ProductClient() {
        this(ConfigManager.getBaseUrl());
    }

    public ProductClient(String baseUrl) {
        this(baseUrl, DEFAULT_READ_TIMEOUT_MILLIS);
    }

    public ProductClient(String baseUrl, int readTimeoutMillis) {
        if (readTimeoutMillis <= 0) {
            throw new IllegalArgumentException("Read timeout must be positive");
        }
        this.baseUrl = ConfigManager.validateBaseUrl(baseUrl);
        this.readTimeoutMillis = readTimeoutMillis;
    }

    private RequestSpecification request() {
        return given()
                .spec(RequestSpecFactory.create(baseUrl))
                .config(config().httpClient(httpClientConfig()
                        .setParam("http.socket.timeout", readTimeoutMillis)));
    }

    public Response getProductById(int productId) {

        return request()
                .pathParam("productId", productId)

                .when()
                .get("/products/{productId}");
    }

    public Response getProducts(int limit) {

        return request()
                .queryParam("limit", limit)

                .when()
                .get("/products");
    }

    public Response getProductsByCategory(String category) {

        return request()
                .pathParam("category", category)

                .when()
                .get("/products/category/{category}");
    }
}
