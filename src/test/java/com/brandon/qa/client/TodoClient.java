package com.brandon.qa.client;

import com.brandon.qa.specification.RequestSpecFactory;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class TodoClient {

    public Response getTodosByUserId(int userId) {
        return given()
                .spec(RequestSpecFactory.create())
                .pathParam("userId", userId)
                .when()
                .get("/todos/user/{userId}");
    }
}