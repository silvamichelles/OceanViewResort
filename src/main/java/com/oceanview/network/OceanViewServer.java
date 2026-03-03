package com.oceanview.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oceanview.service.RoomService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OceanViewServer {

    private static final Logger LOGGER = Logger.getLogger(OceanViewServer.class.getName());
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));

        registerHandlers(server);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down Ocean View server...");
            server.stop(5);
        }));

        server.start();
        LOGGER.info(String.format("Ocean View Web Service started on port %d", PORT));
    }

    private static void registerHandlers(HttpServer server) {
        RoomService roomService = new RoomService();

        // 1. POST /api/auth/login (Login සඳහා)
        server.createContext("/api/auth/login", exchange -> {
            addCorsHeaders(exchange);
            if (handlePreflight(exchange)) return;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    Map<String, String> creds = MAPPER.readValue(exchange.getRequestBody(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>(){});
                    
                    com.oceanview.dao.impl.UserDAOImpl userDAO = new com.oceanview.dao.impl.UserDAOImpl();
                    // UserDAOImpl හි ඇති authenticate method එක ඇමතීම
                    boolean isValid = userDAO.authenticate(creds.get("username"), creds.get("password"));

                    if (isValid) {
                        sendJson(exchange, 200, Map.of("message", "Login successful"));
                    } else {
                        sendError(exchange, 401, "Unauthorized: Invalid credentials");
                    }
                } catch (Exception e) {
                    sendError(exchange, 500, "Server Error");
                }
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        });

        // 2. POST /api/auth/register (Signup සඳහා)
        server.createContext("/api/auth/register", exchange -> {
            addCorsHeaders(exchange);
            if (handlePreflight(exchange)) return;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    Map<String, String> userData = MAPPER.readValue(exchange.getRequestBody(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>(){});
                    
                    com.oceanview.dao.impl.UserDAOImpl userDAO = new com.oceanview.dao.impl.UserDAOImpl();
                    // UserDAOImpl හි ඇති register method එක ඇමතීම
                    boolean isRegistered = userDAO.register(userData.get("username"), userData.get("password"), userData.get("role"));

                    if (isRegistered) {
                        sendJson(exchange, 201, Map.of("message", "User created successfully"));
                    } else {
                        sendError(exchange, 400, "Registration failed. Username may exist.");
                    }
                } catch (Exception e) {
                    sendError(exchange, 500, "Server Error");
                }
            } else {
                sendError(exchange, 405, "Method Not Allowed");
            }
        });

        // 3. GET /api/rooms
        server.createContext("/api/rooms", exchange -> {
            addCorsHeaders(exchange);
            if (handlePreflight(exchange)) return;

            switch (exchange.getRequestMethod().toUpperCase()) {
                case "GET" -> handleRequest(exchange, () -> roomService.getAllRooms());
                default    -> sendError(exchange, 405, "Method Not Allowed");
            }
        });

        // 4. GET /health
        server.createContext("/health", exchange -> {
            addCorsHeaders(exchange);
            sendJson(exchange, 200, Map.of("status", "UP", "service", "OceanView"));
        });
    }

    @FunctionalInterface
    interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private static <T> void handleRequest(HttpExchange exchange, ThrowingSupplier<T> supplier) {
        try {
            sendJson(exchange, 200, supplier.get());
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Internal server error", e);
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object payload) {
        try {
            byte[] responseBytes = MAPPER.writeValueAsBytes(payload);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write response", e);
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) {
        sendJson(exchange, statusCode, Map.of("error", message, "status", statusCode));
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }
}