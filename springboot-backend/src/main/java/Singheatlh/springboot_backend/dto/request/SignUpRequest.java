package Singheatlh.springboot_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpRequest {
    private String id;

    @NotBlank @Email
    private String email;
    private String name;
    private String role;
    private String username;

    // Additional fields specific to Patient or ClinicStaff
    private Integer clinicId; // For ClinicStaff
}
