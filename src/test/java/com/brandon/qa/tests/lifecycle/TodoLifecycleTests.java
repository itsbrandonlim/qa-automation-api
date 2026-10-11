package com.brandon.qa.tests.lifecycle;

import com.brandon.qa.client.LocalTodoClient;
import com.brandon.qa.support.LocalTodoApi;
import com.brandon.qa.testdata.TodoDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TodoLifecycleTests {

    private LocalTodoApi api;
    private LocalTodoClient client;
    private Map<String, Object> payload;
    private Integer createdTodoId;

    @BeforeEach
    void setUp() throws IOException {
        api = new LocalTodoApi();
        api.start();

        client = new LocalTodoClient(api.getBaseUrl());
        payload = TodoDataFactory.newTodo();

        Response response = client.createTodo(payload);

        // Capture the ID before assertions that could fail.
        createdTodoId = response.jsonPath()
                .getObject("id", Integer.class);

        response.then()
                .statusCode(201)
                .body("todo", equalTo(payload.get("todo")))
                .body("completed", equalTo(false));

        assertNotNull(
                createdTodoId,
                "Setup should create a todo with an ID"
        );
    }

    @Test
    @DisplayName("Retrieve a todo created during setup")
    void shouldRetrieveCreatedTodo() {
        client.getTodo(createdTodoId)
                .then()
                .statusCode(200)
                .body("id", equalTo(createdTodoId))
                .body("todo", equalTo(payload.get("todo")))
                .body("completed", equalTo(false));
    }

    @Test
    @DisplayName("Deleted todo is no longer available")
    void shouldDeleteCreatedTodo() {
        client.deleteTodo(createdTodoId)
                .then()
                .statusCode(204);

        client.getTodo(createdTodoId)
                .then()
                .statusCode(404);
    }

    @AfterEach
    void tearDown() {
        try {
            if (client != null && createdTodoId != null) {
                client.deleteTodo(createdTodoId)
                        .then()
                        .statusCode(anyOf(
                                equalTo(204),
                                equalTo(404)
                        ));

                client.getTodo(createdTodoId)
                        .then()
                        .statusCode(404);
            }
        } finally {
            if (api != null) {
                api.close();
            }
        }
    }
}