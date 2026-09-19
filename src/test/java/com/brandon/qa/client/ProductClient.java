package com.brandon.qa.client;

import com.brandon.qa.specification.RequestSpecFactory;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ProductClient {

    public Response getProductById(int productId) {

        return given()
                .spec(RequestSpecFactory.create())
                .pathParam("productId", productId)

                .when()
                .get("/products/{productId}");
    }

    public Response getProducts(int limit) {

        return given()
                .spec(RequestSpecFactory.create())
                .queryParam("limit", limit)

                .when()
                .get("/products");
    }

    public Response getProductsByCategory(String category) {

        return given()
                .spec(RequestSpecFactory.create())
                .pathParam("category", category)

                .when()
                .get("/products/category/{category}");
    }
}