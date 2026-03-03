package com.oceanview.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WebServiceClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── Authentication (Login & Register) ────────────────────────────────────

    public static boolean loginViaApi(String username, String password) throws Exception {
        Map<String, String> credentials = Map.of(
                "username", username,
                "password", password
        );
        String jsonPayload = MAPPER.writeValueAsString(credentials);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    public static boolean registerViaApi(String username, String password, String role) throws Exception {
        Map<String, String> userData = Map.of(
                "username", username,
                "password", password,
                "role", role
        );
        String jsonPayload = MAPPER.writeValueAsString(userData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/register"))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 || response.statusCode() == 201;
    }

    // ── Rooms & Reservations ─────────────────────────────────────────────────

    public static String getRooms() throws Exception {
        return get("/rooms");
    }

    public static <T> List<T> getRooms(TypeReference<List<T>> typeRef) throws Exception {
        return MAPPER.readValue(getRooms(), typeRef);
    }

    public static CompletableFuture<String> getRoomsAsync() {
        return getAsync("/rooms");
    }

    public static String getReservations() throws Exception {
        return get("/reservations");
    }

    public static CompletableFuture<String> getReservationsAsync() {
        return getAsync("/reservations");
    }

    public static String createReservation(String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/reservations"))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return response.body();
    }

    // ── Core HTTP Helpers ────────────────────────────────────────────────────

    private static String get(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return response.body();
    }

    private static CompletableFuture<String> getAsync(String endpoint) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .timeout(TIMEOUT)
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    validateResponseUnchecked(response);
                    return response.body();
                });
    }

    private static void validateResponse(HttpResponse<String> response) throws ApiException {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new ApiException(status, response.body());
        }
    }

    private static void validateResponseUnchecked(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new ApiException(status, response.body());
        }
    }

    public static class ApiException extends RuntimeException {
        private final int statusCode;

        public ApiException(int statusCode, String body) {
            super(String.format("API error %d: %s", statusCode, body));
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}