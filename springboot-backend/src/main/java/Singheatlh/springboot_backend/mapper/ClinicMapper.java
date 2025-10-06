package Singheatlh.springboot_backend.mapper;

import Singheatlh.springboot_backend.dto.ClinicDto;
import Singheatlh.springboot_backend.entity.Clinic;

public class ClinicMapper {

    public static ClinicDto mapToClinicDto(Clinic clinic) {
        return new ClinicDto(
                clinic.getClinicId(),
                clinic.getName(),
                clinic.getAddress(),
                clinic.getTelephoneNumber(),
                clinic.getType(),
                clinic.getOpeningHours(),
                clinic.getClosingHours()
        );
    }

    public static Clinic mapToClinic(ClinicDto clinicDto) {
        return new Clinic(
                clinicDto.getClinicId(),
                clinicDto.getName(),
                clinicDto.getAddress(),
                clinicDto.getTelephoneNumber(),
                clinicDto.getType(),
                clinicDto.getOpeningHours(),
                clinicDto.getClosingHours()
        );
    }
}
