package com.brandon.qa.tests.auth;

import com.brandon.qa.client.AuthClient;
import com.brandon.qa.model.LoginRequest;
import com.brandon.qa.model.LoginResponse;

import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.module.jsv.JsonSchemaValidator
        .matchesJsonSchemaInClasspath;

class AuthTests {

    private static final String VALID_USERNAME = "emilys";
    private static final String VALID_PASSWORD = "emilyspass";

    private final AuthClient authClient = new AuthClient();

    private LoginResponse loginAsDemoUser() {
        LoginRequest request =
                new LoginRequest(VALID_USERNAME, VALID_PASSWORD);

        LoginResponse loginResponse = authClient.login(request)
                .then()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class, ObjectMapperType.JACKSON_2);

        assertNotNull(loginResponse.getAccessToken());
        assertFalse(loginResponse.getAccessToken().isBlank());

        return loginResponse;
    }

    @Test
    void shouldLoginWithValidCredentials() {
        LoginRequest request =
                new LoginRequest(VALID_USERNAME, VALID_PASSWORD);

        Response response = authClient.login(request);

        response.then()
                .statusCode(200);

        LoginResponse loginResponse =
                response.as(LoginResponse.class, ObjectMapperType.JACKSON_2);

        assertAll(
                () -> assertEquals(VALID_USERNAME, loginResponse.getUsername()),
                () -> assertNotNull(loginResponse.getId()),
                () -> assertNotNull(loginResponse.getAccessToken())
        );

        assertFalse(loginResponse.getAccessToken().isBlank());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvSource({
            "wrong password, emilys, definitely-wrong-password",
            "unknown username, qa_user_does_not_exist_982734, emilyspass",
            "both incorrect, qa_user_does_not_exist_982734, definitely-wrong-password"
    })
    void shouldRejectInvalidCredentials(
            String scenario,
            String username,
            String password
    ) {
        LoginRequest request = new LoginRequest(username, password);

        Response response = authClient.login(request);

        response.then()
                .statusCode(400)
                .body("message", equalTo("Invalid credentials"));
    }

    @Test
    void shouldGetCurrentUserWithValidToken() {
        LoginResponse loginResponse = loginAsDemoUser();

        Response response =
                authClient.getCurrentUser(loginResponse.getAccessToken());

        response.then()
                .statusCode(200)
                .body("id", equalTo(loginResponse.getId()))
                .body("username", equalTo(VALID_USERNAME));
    }

    @Test
    void shouldRejectCurrentUserRequestWithoutToken() {
        Response response = authClient.getCurrentUserWithoutToken();

        response.then()
                .statusCode(401)
                .body("message", not(blankOrNullString()));
    }

    @Test
    void shouldMatchLoginResponseSchema() {
        LoginRequest request =
                new LoginRequest(VALID_USERNAME, VALID_PASSWORD);

        authClient.login(request)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(
                        "schemas/login-response-schema.json"
                ));
    }
}