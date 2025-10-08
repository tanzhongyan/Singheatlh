package Singheatlh.springboot_backend.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class PatientDto extends UserDto {
    private List<AppointmentDto> appointments;
}
