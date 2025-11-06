package Singheatlh.springboot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response DTO used across all controllers.
 * Provides consistent error format for API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String timestamp;
    private int status;

    /**
     * Constructor with just message - sets timestamp automatically
     */
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
    }

    /**
     * Constructor with message and status - sets timestamp automatically
     */
    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now().toString();
    }
}
