package com.brandon.qa.tests.workflows;

import com.brandon.qa.client.AuthClient;
import com.brandon.qa.client.TodoClient;
import com.brandon.qa.model.LoginRequest;
import com.brandon.qa.model.LoginResponse;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class UserWorkflowTests {

    private final AuthClient authClient = new AuthClient();
    private final TodoClient todoClient = new TodoClient();

    @Test
    @DisplayName("Login, retrieve profile, and verify the user's todos")
    void shouldRetrieveTodosForLoggedInUser() {
        // Step 1: Log in using DummyJSON's public demo account.
        LoginRequest loginRequest =
                new LoginRequest("emilys", "emilyspass");

        Response loginResponse = authClient.login(loginRequest);

        loginResponse.then()
                .statusCode(200);

        LoginResponse login = loginResponse.as(
                LoginResponse.class,
                ObjectMapperType.JACKSON_2
        );

        assertNotNull(login.getId(), "Login should return a user ID");
        assertEquals(
                loginRequest.getUsername(),
                login.getUsername(),
                "Login should return the requested user"
        );

        String accessToken = login.getAccessToken();

        assertNotNull(accessToken, "Login should return an access token");
        assertFalse(accessToken.isBlank(), "Access token should not be blank");

        // Step 2: Use this login's token to retrieve the profile.
        Response profileResponse =
                authClient.getCurrentUser(accessToken);

        profileResponse.then()
                .statusCode(200)
                .body("id", equalTo(login.getId()))
                .body("username", equalTo(loginRequest.getUsername()));

        int userId = profileResponse.jsonPath().getInt("id");

        // Step 3: Use the profile's ID to retrieve associated todos.
        Response todosResponse = todoClient.getTodosByUserId(userId);

        todosResponse.then()
                .statusCode(200);

        List<Integer> todoUserIds = todosResponse.jsonPath()
                .getList("todos.userId", Integer.class);

        assertNotNull(
                todoUserIds,
                "Response should contain a todos array"
        );

        // An empty list is valid: a user may have no todos.
        for (Integer todoUserId : todoUserIds) {
            assertEquals(
                    Integer.valueOf(userId),
                    todoUserId,
                    "Every returned todo should belong to the selected user"
            );
        }
    }
}