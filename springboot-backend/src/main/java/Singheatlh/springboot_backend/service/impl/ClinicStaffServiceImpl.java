package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.entity.ClinicStaff;
import Singheatlh.springboot_backend.entity.User;
import Singheatlh.springboot_backend.entity.enums.Role;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.ClinicStaffMapper;
import Singheatlh.springboot_backend.repository.ClinicStaffRepository;
import Singheatlh.springboot_backend.service.ClinicStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicStaffServiceImpl implements ClinicStaffService {

    private final ClinicStaffRepository clinicStaffRepository;
    private final ClinicStaffMapper clinicStaffMapper;



    @Override
    public ClinicStaffDto getById(String id) {
        ClinicStaff clinicStaff = clinicStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic staff not found with id: " + id));
        return clinicStaffMapper.toDto(clinicStaff);
    }

    @Override
    public ClinicStaffDto create(ClinicStaffDto clinicStaffDto) {
        ClinicStaff clinicStaff = clinicStaffMapper.toEntity(clinicStaffDto);
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

        // Clinic should be updated through a separate method or service

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
    public void deleteClinicStaffBy(String id) {
        ClinicStaff clinicStaff = clinicStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic staff not found with id: " + id));
        clinicStaffRepository.deleteById(id);
    }

    @Override
    public List<ClinicStaffDto> getClinicStaffByClinic(int clinicId) {
        List<ClinicStaff> staffList = clinicStaffRepository.findByClinicClinicId(clinicId);
        return staffList.stream()
                .map(clinicStaffMapper::toDto)
                .collect(Collectors.toList());

    }

    @Override
    public ClinicStaffDto getByEmail(String email) {
        ClinicStaff clinicStaff = clinicStaffRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundExecption("User not found with email: " + email));
        return clinicStaffMapper.toDto(clinicStaff);
    }
}
