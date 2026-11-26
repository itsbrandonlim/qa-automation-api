package com.brandon.qa.tests.contracts;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TodoSchemaContractTests {

    private static final String SCHEMA = "schemas/todo-response-schema.json";

    @Test
    void shouldAcceptOriginalShapeAndAnAdditiveField() {
        assertThat("""
                {"id":1,"todo":"Practice API contracts","completed":false}
                """, matchesJsonSchemaInClasspath(SCHEMA));
        assertThat("""
                {"id":1,"todo":"Practice API contracts","completed":false,"priority":"low"}
                """, matchesJsonSchemaInClasspath(SCHEMA));
    }

    static Stream<String> incompatibleResponses() {
        return Stream.of(
                // Required field removed.
                """
                {"id":1,"todo":"Practice API contracts"}
                """,
                // Field type changed from integer to string.
                """
                {"id":"1","todo":"Practice API contracts","completed":false}
                """,
                // Field renamed: the consumer still expects "todo".
                """
                {"id":1,"title":"Practice API contracts","completed":false}
                """
        );
    }

    @ParameterizedTest(name = "Reject incompatible response {index}")
    @MethodSource("incompatibleResponses")
    void shouldRejectBreakingShapeChanges(String response) {
        assertThat(response, not(matchesJsonSchemaInClasspath(SCHEMA)));
    }

    @Test
    void shouldShowWhyValidShapeDoesNotGuaranteeCorrectIdentity() {
        String wrongTodo = """
                {"id":999,"todo":"Another user's todo","completed":false}
                """;

        assertThat(wrongTodo, matchesJsonSchemaInClasspath(SCHEMA));
        // A schema accepts this shape. A GET /todos/1 test must also assert the ID is 1.
        assertNotEquals(1, JsonPath.from(wrongTodo).getInt("id"));
    }
}
