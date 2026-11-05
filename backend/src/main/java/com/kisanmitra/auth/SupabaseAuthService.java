package com.kisanmitra.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class SupabaseAuthService {
    private final String projectUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public SupabaseAuthService(
            @Value("${SUPABASE_URL:}") String projectUrl,
            @Value("${SUPABASE_ANON_KEY:${SUPABASE_SERVICE_ROLE_KEY:}}") String apiKey,
            ObjectMapper objectMapper) {
        this.projectUrl = projectUrl == null ? "" : projectUrl.replaceAll("/+$", "");
        this.apiKey = apiKey == null ? "" : apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    }

    public JsonNode signIn(String email, String password) {
        return post("/auth/v1/token?grant_type=password", Map.of("email", email, "password", password));
    }

    public JsonNode signUp(String email, String password, Map<String, Object> metadata) {
        return post("/auth/v1/signup", Map.of("email", email, "password", password, "data", metadata));
    }

    private JsonNode post(String path, Map<String, Object> payload) {
        if (projectUrl.isBlank() || apiKey.isBlank()) {
            throw new SupabaseAuthException(503, "Supabase Auth is not configured on the backend.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(projectUrl + path))
                    .timeout(Duration.ofSeconds(15))
                    .header("apikey", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode body = response.body().isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(response.body());
            if (response.statusCode() / 100 != 2) {
                throw mapError(response.statusCode(), body);
            }
            return body;
        } catch (SupabaseAuthException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SupabaseAuthException(502, "Could not reach Supabase Auth. Please try again.");
        } catch (IOException | IllegalArgumentException e) {
            throw new SupabaseAuthException(502, "Could not reach Supabase Auth. Please try again.");
        }
    }

    private SupabaseAuthException mapError(int status, JsonNode body) {
        String code = body.path("error_code").asText("");
        if ("email_not_confirmed".equals(code)) {
            return new SupabaseAuthException(403, "Please confirm your email before signing in.");
        }
        if (status == 409 || "user_already_exists".equals(code)) {
            return new SupabaseAuthException(409, "An account with this email already exists.");
        }
        if (status == 400 || status == 401 || "invalid_credentials".equals(code)) {
            return new SupabaseAuthException(401, "Invalid email or password.");
        }
        if (status == 429) {
            return new SupabaseAuthException(429, "Too many attempts. Please wait and try again.");
        }
        return new SupabaseAuthException(502, "Supabase Auth could not complete the request. Please try again.");
    }

    public static class SupabaseAuthException extends RuntimeException {
        private final int status;

        public SupabaseAuthException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int status() {
            return status;
        }
    }
}
