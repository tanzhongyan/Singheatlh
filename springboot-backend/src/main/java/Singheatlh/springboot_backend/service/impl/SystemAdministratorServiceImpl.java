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
import Singheatlh.springboot_backend.service.SupabaseAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    private final SupabaseAuthClient supabaseAuthClient;

    @Override
    public SystemAdministratorDto getById(String id) {
        UUID adminId = UUID.fromString(id);
        SystemAdministrator admin = systemAdministratorRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundExecption("System Administrator not found with id: " + id));
        return systemAdministratorMapper.toDto(admin);
    }

    @Override
    public SystemAdministratorDto createSystemAdministrator(SystemAdministratorDto adminDto) {
        if (systemAdministratorRepository.existsById(adminDto.getUserId())) {
            throw new RuntimeException("System Administrator already exists with id: " + adminDto.getUserId());
        }

        SystemAdministrator admin = systemAdministratorMapper.toEntity(adminDto);
        admin.setRole(Role.S);

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
        SystemAdministrator admin = systemAdministratorRepository.findById(adminDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundExecption("System Administrator not found with id: " + adminDto.getUserId()));

        // Update only allowed fields
        admin.setName(adminDto.getName());
        // Email and role should be updated through separate auth service

        SystemAdministrator savedAdmin = systemAdministratorRepository.save(admin);
        return systemAdministratorMapper.toDto(savedAdmin);
    }

    @Override
    public void deleteSystemAdministrator(String id) {
        UUID adminId = UUID.fromString(id);
        if (!systemAdministratorRepository.existsById(adminId)) {
            throw new ResourceNotFoundExecption("System Administrator not found with id: " + id);
        }
        systemAdministratorRepository.deleteById(adminId);
    }

    @Override
    public PatientDto createPatient(CreatePatientRequest request) {
        // Check if user already exists in our database
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // Create user in Supabase Auth if ID is not provided
        String userId = request.getId();
        if (userId == null || userId.trim().isEmpty()) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("name", request.getName());

            try {
                SupabaseAuthClient.SupabaseAuthResponse response = supabaseAuthClient.signUp(
                    request.getEmail(),
                    request.getPassword(),
                    metadata
                );
                userId = response.getUser().getId();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create user in Supabase Auth: " + e.getMessage());
            }
        }

        PatientDto patientDto = PatientDto.builder()
                .userId(UUID.fromString(userId))
                .name(request.getName())
                .email(request.getEmail())
                .telephoneNumber(request.getTelephoneNumber())
                .role(Role.P)
                .build();

        Patient patient = patientMapper.toEntity(patientDto);
        patient.setRole(Role.P);

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toDto(savedPatient);
    }

    @Override
    public ClinicStaffDto createClinicStaff(CreateClinicStaffRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // Create user in Supabase Auth if ID is not provided
        String userId = request.getId();
        if (userId == null || userId.trim().isEmpty()) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("name", request.getName());

            try {
                SupabaseAuthClient.SupabaseAuthResponse response = supabaseAuthClient.signUp(
                    request.getEmail(),
                    request.getPassword(),
                    metadata
                );
                userId = response.getUser().getId();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create user in Supabase Auth: " + e.getMessage());
            }
        }

        ClinicStaffDto staffDto = ClinicStaffDto.builder()
                .userId(UUID.fromString(userId))
                .name(request.getName())
                .email(request.getEmail())
                .telephoneNumber(request.getTelephoneNumber())
                .role(Role.C)
                .clinicId(request.getClinicId())
                .build();

        ClinicStaff clinicStaff = clinicStaffMapper.toEntity(staffDto);
        clinicStaff.setRole(Role.C);

        if (request.getClinicId() != 0) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new RuntimeException("Clinic not found with ID: " + request.getClinicId()));
            clinicStaff.setClinic(clinic);
        }

        ClinicStaff savedStaff = clinicStaffRepository.save(clinicStaff);
        return clinicStaffMapper.toDto(savedStaff);
    }
}
