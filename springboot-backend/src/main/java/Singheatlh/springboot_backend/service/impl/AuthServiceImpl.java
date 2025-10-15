package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.*;
import Singheatlh.springboot_backend.dto.request.SignUpRequest;
import Singheatlh.springboot_backend.dto.request.LoginRequest;
import Singheatlh.springboot_backend.entity.*;
import Singheatlh.springboot_backend.entity.enums.Role;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.*;
import Singheatlh.springboot_backend.repository.*;
import Singheatlh.springboot_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final ClinicStaffRepository clinicStaffRepository;
    private final SystemAdministratorRepository systemAdministratorRepository;
    private final ClinicRepository clinicRepository;

    private final UserMapper userMapper;
    private final PatientMapper patientMapper;
    private final ClinicStaffMapper clinicStaffMapper;
    private final SystemAdministratorMapper systemAdministratorMapper;

    @Override
    @Transactional
    public UserDto signUp(SignUpRequest signUpRequest) {
        log.info("Attempting to sign up user with email: {}", signUpRequest.getEmail());

        // Check if user already exists in our database
        Optional<User> existingUser = userRepository.findByEmail(signUpRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + signUpRequest.getEmail() + " already exists");
        }

        // Note: Supabase Auth creation happens in the frontend
        // The frontend should provide the supabaseUid from the auth response
        if (signUpRequest.getId() == null || signUpRequest.getId().trim().isEmpty()) {
            throw new RuntimeException("Supabase UID is required for user creation");
        }

        // Create the appropriate user type based on role
        UserDto userDto;
        try {
            Role role = Role.valueOf(signUpRequest.getRole().toUpperCase());

            switch (role) {
                case PATIENT:
                    userDto = createPatient(signUpRequest);
                    break;
                case CLINIC_STAFF:
                    userDto = createClinicStaff(signUpRequest);
                    break;
                case SYSTEM_ADMINISTRATOR:
                    userDto = createSystemAdministrator(signUpRequest);
                    break;
                default:
                    throw new RuntimeException("Invalid role: " + signUpRequest.getRole());
            }

            log.info("Successfully created user with ID: {} and role: {}", signUpRequest.getId(), role);
            return userDto;

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + signUpRequest.getRole());
        }
    }

    @Override
    public UserDto login(LoginRequest loginRequest) {
        log.info("Attempting login for email: {}", loginRequest.getEmail());

        // Note: Actual authentication happens in Supabase Auth (frontend)
        // This method just retrieves the user profile after successful auth

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with email: " + loginRequest.getEmail()));

        UserDto userDto = userMapper.toDto(user);
        log.info("Login successful for user: {}", user.getEmail());

        return userDto;
    }

    @Override
    public UserDto getCurrentUserProfile(String supabaseUid) {
        log.debug("Fetching profile for user ID: {}", supabaseUid);

        User user = userRepository.findById(supabaseUid)
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with ID: " + supabaseUid));

        // Return the appropriate DTO based on user type
        return getUserSpecificDto(user);
    }

    @Override
    public boolean validateSupabaseJwt(String jwtToken) {
        // TODO: Implement JWT validation using Supabase's public key
        // For now, return true (you should implement proper JWT validation)
        log.debug("Validating JWT token");
        return true;
    }

    @Override
    public String extractUserIdFromToken(String jwtToken) {
        // TODO: Extract the supabase user ID from the JWT token
        // For now, return a placeholder (you should implement proper JWT parsing)
        log.debug("Extracting user ID from JWT token");
        return "extracted-user-id-from-jwt";
    }

    @Override
    @Transactional
    public void updateEmail(String userId, String newEmail, String currentPassword) {
        log.info("Updating email for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with ID: " + userId));

        // Check if new email is already taken
        Optional<User> existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent() && !existingUser.get().getSupabaseUid().equals(userId)) {
            throw new RuntimeException("Email " + newEmail + " is already taken");
        }

        // Note: In a real implementation, you would:
        // 1. Verify current password with Supabase Auth
        // 2. Update email in Supabase Auth
        // 3. Update email in your database

        user.setEmail(newEmail);
        userRepository.save(user);

        log.info("Email updated successfully for user ID: {}", userId);
    }

    @Override
    public void changePassword(String userId, String currentPassword, String newPassword) {
        log.info("Changing password for user ID: {}", userId);

        // Note: Password changes are handled entirely by Supabase Auth
        // This method would typically coordinate with Supabase Auth API

        // In a real implementation, you would:
        // 1. Verify current password with Supabase Auth
        // 2. Update password in Supabase Auth

        log.info("Password change initiated for user ID: {}", userId);
    }

    @Override
    public void resetPassword(String email) {
        log.info("Initiating password reset for email: {}", email);

        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with email: " + email));

        // Note: Password reset is handled by Supabase Auth
        // This would typically trigger Supabase's password reset flow

        log.info("Password reset initiated for user: {}", email);
    }

    // Helper methods
    private UserDto createPatient(SignUpRequest request) {
        PatientDto patientDto = PatientDto.builder()
                .id(request.getId())
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.PATIENT)
                .appointmentIds(null)
                .build();

        Patient patient = patientMapper.toEntity(patientDto);
        patient.setRole(Role.PATIENT);

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toDto(savedPatient);
    }

    private UserDto createClinicStaff(SignUpRequest request) {
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

        // Set clinic if provided
        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new RuntimeException("Clinic not found with ID: " + request.getClinicId()));
            clinicStaff.setClinic(clinic);
        }

        ClinicStaff savedStaff = clinicStaffRepository.save(clinicStaff);
        return clinicStaffMapper.toDto(savedStaff);
    }

    private UserDto createSystemAdministrator(SignUpRequest request) {
        SystemAdministratorDto adminDto = SystemAdministratorDto.builder()
                .id(request.getId())
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.SYSTEM_ADMINISTRATOR)
                .build();

        SystemAdministrator admin = systemAdministratorMapper.toEntity(adminDto);
        admin.setRole(Role.SYSTEM_ADMINISTRATOR);

        SystemAdministrator savedAdmin = systemAdministratorRepository.save(admin);
        return systemAdministratorMapper.toDto(savedAdmin);
    }

    private UserDto getUserSpecificDto(User user) {
        if (user instanceof Patient) {
            return patientMapper.toDto((Patient) user);
        } else if (user instanceof ClinicStaff) {
            return clinicStaffMapper.toDto((ClinicStaff) user);
        } else if (user instanceof SystemAdministrator) {
            return systemAdministratorMapper.toDto((SystemAdministrator) user);
        } else {
            return userMapper.toDto(user);
        }
    }
}