package Singheatlh.springboot_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Email response from SMU Lab Notification Service
 * Uses { "status": "Email Sent" }
 */
public class EmailResponse {
    
    // Field per current API: { "status": "Email Sent" }
    @JsonProperty("status")
    private String status;
    
    public EmailResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "EmailResponse{" +
                "status='" + status + '\'' +
                '}';
    }
}

