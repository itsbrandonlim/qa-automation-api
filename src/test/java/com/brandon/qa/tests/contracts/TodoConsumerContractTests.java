package com.brandon.qa.tests.contracts;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.brandon.qa.client.LocalTodoClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "LocalTodoApi", hostInterface = "127.0.0.1",
        pactVersion = PactSpecVersion.V3)
@Execution(ExecutionMode.SAME_THREAD)
class TodoConsumerContractTests {

    // Consumer expectations are independent of the provider's implementation.
    private static final String TODO_TEXT = "Practice API contracts";

    @Pact(consumer = "LocalTodoClient")
    public RequestResponsePact createTodo(PactDslWithProvider builder) {
        return builder.given("no todos exist")
                .uponReceiving("create a todo")
                    .path("/todos").method("POST")
                    .headers("Content-Type", "application/json")
                    .body(new PactDslJsonBody()
                            .stringValue("todo", TODO_TEXT)
                            .booleanValue("completed", false))
                .willRespondWith()
                    .status(201)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(new PactDslJsonBody()
                            .integerType("id", 1)
                            .stringValue("todo", TODO_TEXT)
                            .booleanValue("completed", false))
                .toPact();
    }

    @Pact(consumer = "LocalTodoClient")
    public RequestResponsePact retrieveTodo(PactDslWithProvider builder) {
        return builder.given("todo 1 exists")
                .uponReceiving("retrieve todo 1")
                    .path("/todos/1").method("GET")
                .willRespondWith()
                    .status(200)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(new PactDslJsonBody()
                            .numberValue("id", 1)
                            .stringValue("todo", TODO_TEXT)
                            .booleanValue("completed", false))
                .toPact();
    }

    @Pact(consumer = "LocalTodoClient")
    public RequestResponsePact missingTodo(PactDslWithProvider builder) {
        return builder.given("no todos exist")
                .uponReceiving("retrieve a missing todo")
                    .path("/todos/9999").method("GET")
                .willRespondWith()
                    .status(404)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(new PactDslJsonBody().stringValue("error", "Todo not found"))
                .toPact();
    }

    @Pact(consumer = "LocalTodoClient")
    public RequestResponsePact deleteTodo(PactDslWithProvider builder) {
        return builder.given("todo 1 exists")
                .uponReceiving("delete todo 1")
                    .path("/todos/1").method("DELETE")
                .willRespondWith().status(204)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createTodo")
    void shouldCreateTodo(MockServer mockServer) {
        new LocalTodoClient(mockServer.getUrl())
                .createTodo(Map.of("todo", TODO_TEXT, "completed", false))
                .then().statusCode(201)
                .body("id", greaterThan(0))
                .body("todo", equalTo(TODO_TEXT))
                .body("completed", equalTo(false));
    }

    @Test
    @PactTestFor(pactMethod = "retrieveTodo")
    void shouldRetrieveTodo(MockServer mockServer) {
        new LocalTodoClient(mockServer.getUrl()).getTodo(1)
                .then().statusCode(200)
                .body("id", equalTo(1))
                .body("todo", equalTo(TODO_TEXT))
                .body("completed", equalTo(false));
    }

    @Test
    @PactTestFor(pactMethod = "missingTodo")
    void shouldExposeMissingTodo(MockServer mockServer) {
        new LocalTodoClient(mockServer.getUrl()).getTodo(9999)
                .then().statusCode(404)
                .body("error", equalTo("Todo not found"));
    }

    @Test
    @PactTestFor(pactMethod = "deleteTodo")
    void shouldDeleteTodo(MockServer mockServer) {
        new LocalTodoClient(mockServer.getUrl()).deleteTodo(1)
                .then().statusCode(204)
                .body(isEmptyString());
    }
}
