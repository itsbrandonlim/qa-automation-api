package com.brandon.qa.testdata;

import java.util.Map;
import java.util.UUID;

public final class TodoDataFactory {

    private TodoDataFactory() {
    }

    public static Map<String, Object> newTodo() {
        return Map.of(
                "todo", "qa-todo-" + UUID.randomUUID(),
                "completed", false
        );
    }
}