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
    public ResponseEntity<?> getCurrentUserProfile(@AuthenticationPrincipal String supabaseUid) {
        log.debug("Fetching profile for user ID: {}", supabaseUid);

        try {
            UserDto userDto = authService.getCurrentUserProfile(supabaseUid);
            return ResponseEntity.ok(userDto);

        } catch (RuntimeException e) {
            log.error("Failed to fetch profile for user ID: {}", supabaseUid, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
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

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class TokenValidationResponse {
        private boolean valid;
        private String userId;

        public TokenValidationResponse(boolean valid, String userId) {
            this.valid = valid;
            this.userId = userId;
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}