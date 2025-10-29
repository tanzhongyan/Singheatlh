package Singheatlh.springboot_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with Supabase Auth API
 * Handles user creation, authentication, and profile management
 */
@Slf4j
@Service
public class SupabaseAuthClient {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String supabaseKey;

    public SupabaseAuthClient(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.anon.key}") String supabaseKey,
            WebClient.Builder webClientBuilder) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        this.webClient = webClientBuilder
                .baseUrl(supabaseUrl + "/auth/v1")
                .defaultHeader("apikey", supabaseKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Create a new user in Supabase Auth
     * This will trigger the handle_new_user() function via the auth trigger
     *
     * @param email User's email
     * @param password User's password
     * @param metadata Additional user metadata (name, etc.)
     * @return SupabaseAuthResponse containing user ID and session info
     */
    public SupabaseAuthResponse signUp(String email, String password, Map<String, Object> metadata) {
        log.info("Creating Supabase Auth user for email: {}", email);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);
        if (metadata != null && !metadata.isEmpty()) {
            requestBody.put("data", metadata);
        }

        try {
            SupabaseAuthResponse response = webClient.post()
                    .uri("/signup")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(SupabaseAuthResponse.class)
                    .block();

            log.info("Successfully created Supabase Auth user with ID: {}", response.getUser().getId());
            return response;

        } catch (WebClientResponseException e) {
            log.error("Failed to create Supabase Auth user: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create user in Supabase Auth: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during Supabase Auth signup", e);
            throw new RuntimeException("Failed to create user in Supabase Auth: " + e.getMessage());
        }
    }

    /**
     * Sign in a user with email and password
     */
    public SupabaseAuthResponse signIn(String email, String password) {
        log.info("Signing in Supabase Auth user: {}", email);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        try {
            return webClient.post()
                    .uri("/token?grant_type=password")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(SupabaseAuthResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to sign in: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Invalid credentials");
        }
    }

    /**
     * Update user email in Supabase Auth
     */
    public void updateEmail(String accessToken, String newEmail) {
        log.info("Updating user email in Supabase Auth");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", newEmail);

        try {
            webClient.put()
                    .uri("/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to update email: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to update email in Supabase Auth");
        }
    }

    /**
     * Update user password in Supabase Auth
     */
    public void updatePassword(String accessToken, String newPassword) {
        log.info("Updating user password in Supabase Auth");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("password", newPassword);

        try {
            webClient.put()
                    .uri("/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to update password: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to update password in Supabase Auth");
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email) {
        log.info("Sending password reset email for: {}", email);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);

        try {
            webClient.post()
                    .uri("/recover")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to send password reset email: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    // Response DTOs
    public static class SupabaseAuthResponse {
        private SupabaseUser user;

        @com.fasterxml.jackson.annotation.JsonProperty("access_token")
        private String accessToken;

        @com.fasterxml.jackson.annotation.JsonProperty("refresh_token")
        private String refreshToken;

        public SupabaseUser getUser() {
            return user;
        }

        public void setUser(SupabaseUser user) {
            this.user = user;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    public static class SupabaseUser {
        private String id;
        private String email;

        @com.fasterxml.jackson.annotation.JsonProperty("user_metadata")
        private Map<String, Object> userMetadata;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Map<String, Object> getUserMetadata() {
            return userMetadata;
        }

        public void setUserMetadata(Map<String, Object> userMetadata) {
            this.userMetadata = userMetadata;
        }
    }
}
