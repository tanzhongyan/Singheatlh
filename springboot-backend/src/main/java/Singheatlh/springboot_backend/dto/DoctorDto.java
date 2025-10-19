package Singheatlh.springboot_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorDto {
    private Long doctorId;

    @NotBlank(message = "Doctor name is required")
    private String name;
    private String schedule;
    private Integer clinicId;
}
