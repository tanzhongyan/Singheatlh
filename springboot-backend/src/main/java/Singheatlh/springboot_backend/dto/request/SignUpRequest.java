package Singheatlh.springboot_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpRequest {
    private String id;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    // Optional user profile fields
    private String name;  // Full Name (Optional)
    private String telephoneNumber;  // Phone Number (Optional)

    private String role;
    private String username;

    // Additional fields specific to Patient or ClinicStaff
    private Integer clinicId; // For ClinicStaff
}
