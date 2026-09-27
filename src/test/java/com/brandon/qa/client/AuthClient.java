package com.brandon.qa.client;

import com.brandon.qa.model.LoginRequest;
import com.brandon.qa.specification.RequestSpecFactory;

import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AuthClient {

    public Response login(LoginRequest request) {

        return given()
                .spec(RequestSpecFactory.create())
                .contentType(ContentType.JSON)
                .body(request, ObjectMapperType.JACKSON_2)
                .when()
                .post("/auth/login");
    }

    public Response getCurrentUser(String accessToken) {

        return given()
                .spec(RequestSpecFactory.create())
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/auth/me");
    }

    public Response getCurrentUserWithoutToken() {

        return given()
                .spec(RequestSpecFactory.create())
                .when()
                .get("/auth/me");
    }
}