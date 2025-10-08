package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.entity.ClinicStaff;
import Singheatlh.springboot_backend.entity.enums.Role;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.ClinicStaffMapper;
import Singheatlh.springboot_backend.repository.ClinicStaffRepository;
import Singheatlh.springboot_backend.service.ClinicStaffService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClinicStaffServiceImpl implements ClinicStaffService {

    private final ClinicStaffRepository clinicStaffRepository;
    private final ClinicStaffMapper clinicStaffMapper;

    // ✅ Constructor injection (preferred)
    public ClinicStaffServiceImpl(ClinicStaffRepository clinicStaffRepository, ClinicStaffMapper clinicStaffMapper) {
        this.clinicStaffRepository = clinicStaffRepository;
        this.clinicStaffMapper = clinicStaffMapper;
    }

    @Override
    public ClinicStaffDto getById(Long id) {
        ClinicStaff clinicStaff = clinicStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic staff not found with id: " + id));
        return clinicStaffMapper.toDto(clinicStaff);
    }

    @Override
    public ClinicStaffDto create(ClinicStaffDto clinicStaffDto, String hashedPassword) {
        ClinicStaff clinicStaff = clinicStaffMapper.toEntity(clinicStaffDto);
        clinicStaff.setHashedPassword(hashedPassword);
        clinicStaff.setRole(Role.CLINIC_STAFF);
        ClinicStaff savedStaff = clinicStaffRepository.save(clinicStaff);
        return clinicStaffMapper.toDto(savedStaff);
    }

    @Override
    public ClinicStaffDto update(ClinicStaffDto clinicStaffDto) {
        ClinicStaff clinicStaff = clinicStaffRepository.findById(clinicStaffDto.getId())
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic staff not found with id: " + clinicStaffDto.getId()));

        // ✅ Update fields (avoid overwriting clinic reference unless provided)
        clinicStaff.setName(clinicStaffDto.getName());
        clinicStaff.setUsername(clinicStaffDto.getUsername());
        clinicStaff.setEmail(clinicStaffDto.getEmail());

        if (clinicStaffDto.getClinicDto() != null) {
            clinicStaff.setClinic(
                    clinicStaffMapper.toEntity(clinicStaffDto).getClinic()
                    // alternatively: clinicStaff.setClinic(clinicStaffDto.getClinic());
            );
        }

        ClinicStaff updatedStaff = clinicStaffRepository.save(clinicStaff);
        return clinicStaffMapper.toDto(updatedStaff);
    }

    @Override
    public List<ClinicStaffDto> getAllClinicStaff() {
        List<ClinicStaff> staffList = clinicStaffRepository.findAll();
        return staffList.stream()
                .map(clinicStaffMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClinicStaffDto> getClinicStaffByName(String name) {
        List<ClinicStaff> staffList = clinicStaffRepository.findByNameContainingIgnoreCase(name);
        return staffList.stream()
                .map(clinicStaffMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteClinicStaffBy(Long id) {
        ClinicStaff clinicStaff = clinicStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic staff not found with id: " + id));
        clinicStaffRepository.delete(clinicStaff);
    }
}
