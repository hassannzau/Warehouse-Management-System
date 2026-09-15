package org.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.exception.ApiException;
import org.example.response.ApiErrorResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

//Thin HTTP + JSON layer the JavaFX desktop client uses to talk to the Spring Boot

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Set once after a successful login; attached as a bearer token on every
    // subsequent request. Held in memory only - never written to disk.
    private volatile String authToken;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public <T> T get(String path, TypeReference<T> responseType) {
        HttpRequest request = requestBuilder(path).GET().build();
        return send(request, responseType);
    }

    public <T> T post(String path, Object body, TypeReference<T> responseType) {
        HttpRequest request = requestBuilder(path).POST(jsonBody(body)).build();
        return send(request, responseType);
    }

    public void post(String path, Object body) {
        HttpRequest request = requestBuilder(path).POST(jsonBody(body)).build();
        send(request);
    }

    public void put(String path, Object body) {
        HttpRequest request = requestBuilder(path).PUT(jsonBody(body)).build();
        send(request);
    }

    public void delete(String path) {
        HttpRequest request = requestBuilder(path).DELETE().build();
        send(request);
    }

    private HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15));
        String token = authToken;
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private HttpRequest.BodyPublisher jsonBody(Object body) {
        try {
            return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
        } catch (IOException e) {
            throw new ApiException(0, "Failed to serialize request: " + e.getMessage());
        }
    }

    private <T> T send(HttpRequest request, TypeReference<T> responseType) {
        String body = send(request);
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, responseType);
        } catch (IOException e) {
            throw new ApiException(0, "Failed to parse response from " + request.uri() + ": " + e.getMessage());
        }
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            throw new ApiException(response.statusCode(), extractMessage(response));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ApiException(0, "Could not reach the server: " + e.getMessage());
        }
    }

    private String extractMessage(HttpResponse<String> response) {
        try {
            ApiErrorResponse error = objectMapper.readValue(response.body(), ApiErrorResponse.class);
            if (error.getMessage() != null && !error.getMessage().isBlank()) {
                return error.getMessage();
            }
        } catch (IOException ignored) {
            // Fall through to a generic message below.
        }
        return "Request failed with status " + response.statusCode();
    }
}
