package com.brandon.qa.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalTodoApi implements AutoCloseable {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, ObjectNode> todos =
            new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger();

    private HttpServer server;

    public void start() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );

        server.createContext("/todos", this::handle);
        server.start();
    }

    public String getBaseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("/todos".equals(path) && "POST".equals(method)) {
                createTodo(exchange);
                return;
            }

            if (!path.matches("/todos/\\d+")) {
                sendJson(exchange, 404, Map.of("error", "Not found"));
                return;
            }

            int id;
            try {
                id = Integer.parseInt(
                        path.substring("/todos/".length())
                );
            } catch (NumberFormatException exception) {
                sendJson(exchange, 400, Map.of("error", "Invalid ID"));
                return;
            }

            switch (method) {
                case "GET" -> {
                    ObjectNode todo = todos.get(id);

                    if (todo == null) {
                        sendJson(
                                exchange, 404,
                                Map.of("error", "Todo not found")
                        );
                    } else {
                        sendJson(exchange, 200, todo);
                    }
                }

                case "DELETE" -> {
                    if (todos.remove(id) == null) {
                        sendJson(
                                exchange, 404,
                                Map.of("error", "Todo not found")
                        );
                    } else {
                        exchange.sendResponseHeaders(204, -1);
                    }
                }

                default -> sendJson(
                        exchange, 405,
                        Map.of("error", "Method not allowed")
                );
            }
        } finally {
            exchange.close();
        }
    }

    private void createTodo(HttpExchange exchange) throws IOException {
        JsonNode request;

        try {
            request = mapper.readTree(exchange.getRequestBody());
        } catch (JsonProcessingException exception) {
            sendJson(exchange, 400, Map.of("error", "Invalid JSON"));
            return;
        }

        if (request == null
                || !request.isObject()
                || !request.path("todo").isTextual()
                || request.path("todo").asText().isBlank()
                || !request.path("completed").isBoolean()) {
            sendJson(
                    exchange, 400,
                    Map.of("error", "todo and completed are required")
            );
            return;
        }

        int id = nextId.incrementAndGet();

        ObjectNode todo = mapper.createObjectNode();
        todo.put("id", id);
        todo.put("todo", request.get("todo").asText());
        todo.put("completed", request.get("completed").asBoolean());

        todos.put(id, todo);
        sendJson(exchange, 201, todo);
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            Object body
    ) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);

        exchange.getResponseHeaders()
                .set("Content-Type", "application/json");

        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    @Override
    public void close() {
        if (server != null) {
            server.stop(0);
        }

        todos.clear();
    }
}