package Singheatlh.springboot_backend.mapper;

import org.springframework.stereotype.Component;

import Singheatlh.springboot_backend.dto.ClinicDto;
import Singheatlh.springboot_backend.entity.Clinic;
import org.springframework.stereotype.Component;

@Component
public class ClinicMapper {

    public ClinicDto toDto(Clinic clinic) {
        if (clinic == null) {
            return null;
        }

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

    public Clinic toEntity(ClinicDto clinicDto) {
        if (clinicDto == null) {
            return null;
        }

        return new Clinic(
                clinicDto.getClinicId(),
                clinicDto.getName(),
                clinicDto.getAddress(),
                clinicDto.getTelephoneNumber(),
                clinicDto.getType(),
                clinicDto.getOpeningHours(),
                clinicDto.getClosingHours(),
                null, // appointmentSlotDuration
                null  // doctors
        );
    }
}
