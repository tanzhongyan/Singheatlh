package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.dto.UserDto;
import Singheatlh.springboot_backend.dto.request.*;
import Singheatlh.springboot_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        log.info("=== Health check endpoint called ===");
        return ResponseEntity.ok(new MessageResponse("Auth API is running"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("Received signup request for email: {}", signUpRequest.getEmail());

        try {
            UserDto userDto = authService.signUp(signUpRequest);
            log.info("User successfully registered with ID: {}", userDto.getUserId());

            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);

        } catch (RuntimeException e) {
            log.error("Signup failed for email: {}", signUpRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for email: {}", loginRequest.getEmail());

        try {
            UserDto userDto = authService.login(loginRequest);
            log.info("User successfully logged in: {}", loginRequest.getEmail());

            return ResponseEntity.ok(userDto);

        } catch (RuntimeException e) {
            log.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("=== GET /api/auth/profile called ===");
        log.info("Authorization header: {}", authHeader);

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Missing or invalid Authorization header"));
            }

            // Extract JWT token
            String jwtToken = authHeader.substring(7);
            log.info("JWT Token extracted: {}...", jwtToken.substring(0, Math.min(20, jwtToken.length())));

            // TODO: Properly decode and validate JWT token
            // For now, we'll extract the 'sub' claim which contains the user ID
            String supabaseUid = extractUserIdFromJwt(jwtToken);
            log.info("Extracted user ID from JWT: {}", supabaseUid);

            if (supabaseUid == null) {
                log.error("Could not extract user ID from JWT token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid JWT token"));
            }

            UserDto userDto = authService.getCurrentUserProfile(supabaseUid);
            log.info("Successfully fetched profile for user: {}", userDto.getEmail());
            return ResponseEntity.ok(userDto);

        } catch (RuntimeException e) {
            log.error("Failed to fetch profile", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Helper method to extract user ID from JWT token
    private String extractUserIdFromJwt(String jwtToken) {
        try {
            // JWT format: header.payload.signature
            String[] parts = jwtToken.split("\\.");
            if (parts.length != 3) {
                log.error("Invalid JWT format");
                return null;
            }

            // Decode the payload (base64url encoded)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            log.debug("JWT Payload: {}", payload);

            // Extract 'sub' claim (user ID) from JSON
            // Simple JSON parsing - in production use a proper JWT library
            int subIndex = payload.indexOf("\"sub\":\"");
            if (subIndex == -1) {
                log.error("'sub' claim not found in JWT");
                return null;
            }

            int startIndex = subIndex + 7; // length of "\"sub\":\""
            int endIndex = payload.indexOf("\"", startIndex);
            String userId = payload.substring(startIndex, endIndex);

            return userId;
        } catch (Exception e) {
            log.error("Error extracting user ID from JWT", e);
            return null;
        }
    }

    @PutMapping("/email")
    public ResponseEntity<?> updateEmail(@AuthenticationPrincipal String supabaseUid,
            @RequestBody UpdateEmailRequest updateRequest) {
        log.info("Updating email for user ID: {}", supabaseUid);

        try {
            authService.updateEmail(supabaseUid, updateRequest.getNewEmail(), updateRequest.getCurrentPassword());
            return ResponseEntity.ok(new MessageResponse("Email updated successfully"));

        } catch (RuntimeException e) {
            log.error("Email update failed for user ID: {}", supabaseUid, e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal String supabaseUid,
            @RequestBody ChangePasswordRequest changeRequest) {
        log.info("Changing password for user ID: {}", supabaseUid);

        try {
            authService.changePassword(supabaseUid, changeRequest.getCurrentPassword(), changeRequest.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("Password change initiated"));

        } catch (RuntimeException e) {
            log.error("Password change failed for user ID: {}", supabaseUid, e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetRequest) {
        log.info("Initiating password reset for email: {}", resetRequest.getEmail());

        try {
            authService.resetPassword(resetRequest.getEmail());
            return ResponseEntity.ok(new MessageResponse("Password reset instructions sent to your email"));

        } catch (RuntimeException e) {
            log.error("Password reset failed for email: {}", resetRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody TokenValidationRequest tokenRequest) {
        log.debug("Validating JWT token");

        try {
            boolean isValid = authService.validateSupabaseJwt(tokenRequest.getToken());
            if (isValid) {
                String userId = authService.extractUserIdFromToken(tokenRequest.getToken());
                return ResponseEntity.ok(new TokenValidationResponse(true, userId));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenValidationResponse(false, null));
            }

        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, null));
        }
    }

    // Response DTOs
    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class TokenValidationResponse {
        private boolean valid;
        private String userId;

        public TokenValidationResponse(boolean valid, String userId) {
            this.valid = valid;
            this.userId = userId;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}