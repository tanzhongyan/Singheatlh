package Singheatlh.springboot_backend.mapper;

import org.springframework.stereotype.Component;
import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.dto.ClinicDto;
import Singheatlh.springboot_backend.entity.ClinicStaff;
import Singheatlh.springboot_backend.entity.Clinic;

@Component
public class ClinicStaffMapper {

    private final ClinicMapper clinicMapper;

    public ClinicStaffMapper(ClinicMapper clinicMapper) {
        this.clinicMapper = clinicMapper;
    }

    public ClinicStaffDto toDto(ClinicStaff clinicStaff) {
        if (clinicStaff == null) return null;

        ClinicStaffDto dto = new ClinicStaffDto();
        dto.setId(clinicStaff.getId());
        dto.setUsername(clinicStaff.getUsername());
        dto.setName(clinicStaff.getName());
        dto.setEmail(clinicStaff.getEmail());
        dto.setRole(clinicStaff.getRole());

        // ✅ Convert Clinic entity to ClinicDto using injected ClinicMapper
        Clinic clinic = clinicStaff.getClinic();
        if (clinic != null) {
            ClinicDto clinicDto = clinicMapper.toDto(clinic);
            dto.setClinicDto(clinicDto);
        }

        return dto;
    }

    public ClinicStaff toEntity(ClinicStaffDto dto) {
        if (dto == null) return null;

        ClinicStaff clinicStaff = new ClinicStaff();
        clinicStaff.setId(dto.getId());
        clinicStaff.setUsername(dto.getUsername());
        clinicStaff.setName(dto.getName());
        clinicStaff.setEmail(dto.getEmail());
        clinicStaff.setRole(dto.getRole());


        ClinicDto clinicDto = dto.getClinicDto();
        if (clinicDto != null) {
            Clinic clinic = clinicMapper.toEntity(clinicDto);
            clinicStaff.setClinic(clinic);
        }

        return clinicStaff;
    }
}
