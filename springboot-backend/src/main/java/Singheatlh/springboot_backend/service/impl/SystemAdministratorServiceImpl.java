package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.dto.PatientDto;
import Singheatlh.springboot_backend.dto.SystemAdministratorDto;
import Singheatlh.springboot_backend.dto.request.CreateClinicStaffRequest;
import Singheatlh.springboot_backend.dto.request.CreatePatientRequest;
import Singheatlh.springboot_backend.entity.*;
import Singheatlh.springboot_backend.entity.enums.Role;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.ClinicStaffMapper;
import Singheatlh.springboot_backend.mapper.PatientMapper;
import Singheatlh.springboot_backend.mapper.SystemAdministratorMapper;
import Singheatlh.springboot_backend.repository.*;
import Singheatlh.springboot_backend.service.SystemAdministratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemAdministratorServiceImpl implements SystemAdministratorService {
    private final SystemAdministratorRepository systemAdministratorRepository;
    private final SystemAdministratorMapper systemAdministratorMapper;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final ClinicStaffRepository clinicStaffRepository;
    private final ClinicRepository clinicRepository;
    private final PatientMapper patientMapper;
    private final ClinicStaffMapper clinicStaffMapper;

    @Override
    public SystemAdministratorDto getById(String id) {
        SystemAdministrator admin = systemAdministratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExecption("System Administrator not found with id: " + id));
        return systemAdministratorMapper.toDto(admin);
    }

    @Override
    public SystemAdministratorDto createSystemAdministrator(SystemAdministratorDto adminDto) {
        if (systemAdministratorRepository.existsById(adminDto.getId())) {
            throw new RuntimeException("System Administrator already exists with id: " + adminDto.getId());
        }

        SystemAdministrator admin = systemAdministratorMapper.toEntity(adminDto);
        admin.setRole(Role.SYSTEM_ADMINISTRATOR);

        SystemAdministrator savedAdmin = systemAdministratorRepository.save(admin);
        return systemAdministratorMapper.toDto(savedAdmin);
    }

    @Override
    public List<SystemAdministratorDto> getAllSystemAdministrators() {
        List<SystemAdministrator> admins = systemAdministratorRepository.findAll();
        return admins.stream()
                .map(systemAdministratorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SystemAdministratorDto updateSystemAdministrator(SystemAdministratorDto adminDto) {
        SystemAdministrator admin = systemAdministratorRepository.findById(adminDto.getId())
                .orElseThrow(() -> new ResourceNotFoundExecption("System Administrator not found with id: " + adminDto.getId()));

        // Update only allowed fields
        admin.setName(adminDto.getName());
        admin.setUsername(adminDto.getUsername());
        // Email and role should be updated through separate auth service

        SystemAdministrator savedAdmin = systemAdministratorRepository.save(admin);
        return systemAdministratorMapper.toDto(savedAdmin);
    }

    @Override
    public void deleteSystemAdministrator(String id) {
        if (!systemAdministratorRepository.existsById(id)) {
            throw new ResourceNotFoundExecption("System Administrator not found with id: " + id);
        }
        systemAdministratorRepository.deleteById(id);
    }

    @Override
    public PatientDto createPatient(CreatePatientRequest request) {
        // Check if user already exists in our database
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        if (request.getId() == null || request.getId().trim().isEmpty()) {
            throw new RuntimeException("Supabase UID is required for user creation");
        }

        PatientDto patientDto = PatientDto.builder()
                .id(request.getId())
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.PATIENT)
                .build();

        Patient patient = patientMapper.toEntity(patientDto);
        patient.setRole(Role.PATIENT);

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toDto(savedPatient);
    }

    @Override
    public ClinicStaffDto createClinicStaff(CreateClinicStaffRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        if (request.getId() == null || request.getId().trim().isEmpty()) {
            throw new RuntimeException("Supabase UID is required for user creation");
        }

        ClinicStaffDto staffDto = ClinicStaffDto.builder()
                .id(request.getId())
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.CLINIC_STAFF)
                .clinicId(request.getClinicId())
                .build();

        ClinicStaff clinicStaff = clinicStaffMapper.toEntity(staffDto);
        clinicStaff.setRole(Role.CLINIC_STAFF);

        if (request.getClinicId() != 0) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new RuntimeException("Clinic not found with ID: " + request.getClinicId()));
            clinicStaff.setClinic(clinic);
        }

        ClinicStaff savedStaff = clinicStaffRepository.save(clinicStaff);
        return clinicStaffMapper.toDto(savedStaff);
    }
}
