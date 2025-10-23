package Singheatlh.springboot_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DoctorDto {
    private String doctorId;

    @NotBlank(message = "Doctor name is required")
    private String name;

    @NotNull(message = "Clinic ID is required")
    private Integer clinicId;

    private String clinicName; // For response convenience
}
