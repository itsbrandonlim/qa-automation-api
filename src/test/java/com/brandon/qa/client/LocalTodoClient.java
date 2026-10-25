package com.brandon.qa.client;

import com.brandon.qa.specification.RequestSpecFactory;

import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class LocalTodoClient {

    private final String baseUrl;

    public LocalTodoClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private RequestSpecification request() {
        return given()
                .spec(RequestSpecFactory.create(baseUrl));
    }

    public Response createTodo(Map<String, Object> payload) {
        return request()
                .contentType(ContentType.JSON)
                .body(payload, ObjectMapperType.JACKSON_2)
                .when()
                .post("/todos");
    }

    public Response getTodo(int id) {
        return request()
                .pathParam("id", id)
                .when()
                .get("/todos/{id}");
    }

    public Response deleteTodo(int id) {
        return request()
                .pathParam("id", id)
                .when()
                .delete("/todos/{id}");
    }
}
