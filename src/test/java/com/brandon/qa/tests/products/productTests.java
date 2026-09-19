package com.brandon.qa.tests.products;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class productTests {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://dummyjson.com";
    }

    @Test
    void shouldGetProductById(){

        given()
            .pathParam("productId", 1)

        .when()
            .get("/products/{productId}")

        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("title", notNullValue())
            .body("price", notNullValue());
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist(){
        
        given()
            .pathParam("productId", 9999)

        .when()
            .get("/products/{productId}")

        .then()
            .statusCode(404);
    } 

    @Test
    void shouldReturnProductsFromRequestedCategory(){

        given()
            .pathParam("category", "smartphones")

        .when()
            .get("/products/category/{category}")

        .then()
            .statusCode(200)
            .body("products", not(empty()))
            .body("products[0].category", equalTo("smartphones"));
    }
}