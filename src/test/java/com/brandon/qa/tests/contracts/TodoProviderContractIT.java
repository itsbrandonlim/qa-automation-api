package com.brandon.qa.tests.contracts;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.brandon.qa.client.LocalTodoClient;
import com.brandon.qa.support.LocalTodoApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

@Provider("LocalTodoApi")
@PactFolder("target/pacts")
@Execution(ExecutionMode.SAME_THREAD)
class TodoProviderContractIT {

    private LocalTodoApi api;

    @BeforeEach
    void startProvider(PactVerificationContext context) throws IOException {
        api = new LocalTodoApi();
        api.start();
        URI address = URI.create(api.getBaseUrl());
        context.setTarget(new HttpTestTarget(address.getHost(), address.getPort()));
    }

    @State("no todos exist")
    void emptyProvider() {
        // A new real provider is started for every interaction, so no prior data exists.
    }

    @State("todo 1 exists")
    void existingTodo() {
        new LocalTodoClient(api.getBaseUrl())
                .createTodo(Map.of("todo", "Practice API contracts", "completed", false))
                .then().statusCode(201)
                .body("id", equalTo(1));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyConsumerContract(PactVerificationContext context) {
        // Pact sends the generated request to LocalTodoApi, never to a WireMock stub.
        context.verifyInteraction();
    }

    @AfterEach
    void stopProvider() {
        if (api != null) {
            api.close();
        }
    }
}
