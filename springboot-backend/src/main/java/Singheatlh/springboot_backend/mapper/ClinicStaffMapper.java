package Singheatlh.springboot_backend.mapper;

import org.springframework.stereotype.Component;
import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.dto.ClinicDto;
import Singheatlh.springboot_backend.entity.ClinicStaff;
import Singheatlh.springboot_backend.entity.Clinic;

@Component
public class ClinicStaffMapper {


    public ClinicStaffDto toDto(ClinicStaff clinicStaff) {
        if (clinicStaff == null) return null;

        ClinicStaffDto dto = new ClinicStaffDto();
        dto.setUserId(clinicStaff.getUserId());
        dto.setName(clinicStaff.getName());
        dto.setEmail(clinicStaff.getEmail());
        dto.setRole(clinicStaff.getRole());
        dto.setTelephoneNumber(clinicStaff.getTelephoneNumber());
        dto.setClinicId(clinicStaff.getClinicId());

        return dto;
    }

    public ClinicStaff toEntity(ClinicStaffDto dto) {
        if (dto == null) return null;

        ClinicStaff clinicStaff = new ClinicStaff();
        clinicStaff.setUserId(dto.getUserId());
        clinicStaff.setName(dto.getName());
        clinicStaff.setEmail(dto.getEmail());
        clinicStaff.setRole(dto.getRole());
        clinicStaff.setTelephoneNumber(dto.getTelephoneNumber());
        clinicStaff.setClinicId(dto.getClinicId());

        return clinicStaff;
    }
}
