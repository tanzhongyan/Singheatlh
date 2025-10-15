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
        dto.setId(clinicStaff.getSupabaseUid());
        dto.setUsername(clinicStaff.getUsername());
        dto.setName(clinicStaff.getName());
        dto.setEmail(clinicStaff.getEmail());
        dto.setRole(clinicStaff.getRole());



        return dto;
    }

    public ClinicStaff toEntity(ClinicStaffDto dto) {
        if (dto == null) return null;

        ClinicStaff clinicStaff = new ClinicStaff();
        clinicStaff.setSupabaseUid(dto.getId());
        clinicStaff.setUsername(dto.getUsername());
        clinicStaff.setName(dto.getName());
        clinicStaff.setEmail(dto.getEmail());
        clinicStaff.setRole(dto.getRole());


        return clinicStaff;
    }
}
