package Singheatlh.springboot_backend.dto;

import Singheatlh.springboot_backend.entity.Clinic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ClinicStaffDto extends UserDto{
    private ClinicDto clinicDto;

}
