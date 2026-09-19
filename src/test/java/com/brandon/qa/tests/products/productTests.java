package com.brandon.qa.tests.products;

import com.brandon.qa.client.ProductClient;

import io.restassured.response.Response;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class ProductTests {

    private final ProductClient productClient = new ProductClient();

    @Test
    void shouldGetProductById() {

        Response response = productClient.getProductById(1);

        response.then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", notNullValue())
                .body("price", notNullValue());
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() {

        Response response = productClient.getProductById(9999);

        response.then()
                .statusCode(404);
    }

    @Test
    void shouldReturnProductsFromRequestedCategory() {

        Response response =
                productClient.getProductsByCategory("smartphones");

        response.then()
                .statusCode(200)
                .body("products", not(empty()))
                .body(
                        "products.category",
                        everyItem(equalTo("smartphones"))
                );
    }

    @Test
    void shouldLimitNumberOfProducts() {

        Response response = productClient.getProducts(5);

        response.then()
                .statusCode(200)
                .body("products", hasSize(5))
                .body("limit", equalTo(5));
    }
}