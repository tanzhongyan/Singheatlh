package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.*;
import Singheatlh.springboot_backend.dto.request.SignUpRequest;
import Singheatlh.springboot_backend.dto.request.LoginRequest;
import Singheatlh.springboot_backend.dto.response.JwtResponse;
import Singheatlh.springboot_backend.entity.*;
import Singheatlh.springboot_backend.entity.enums.Role;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.*;
import Singheatlh.springboot_backend.repository.*;
import Singheatlh.springboot_backend.service.AuthService;
import Singheatlh.springboot_backend.service.SupabaseAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    private final SupabaseAuthClient supabaseAuthClient;

    @Override
    @Transactional
    public JwtResponse signUp(SignUpRequest signUpRequest) {
        log.info("Attempting to sign up user with email: {}", signUpRequest.getEmail());

        // Check if user already exists in our database
        Optional<User> existingUser = userRepository.findByEmail(signUpRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + signUpRequest.getEmail() + " already exists");
        }

        // Validate password is provided
        if (signUpRequest.getPassword() == null || signUpRequest.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required for user creation");
        }

        // Create user in Supabase Auth - this will trigger handle_new_user() which creates User_Profile
        Map<String, Object> metadata = new HashMap<>();
        if (signUpRequest.getName() != null && !signUpRequest.getName().trim().isEmpty()) {
            metadata.put("name", signUpRequest.getName());
        }
        if (signUpRequest.getTelephoneNumber() != null && !signUpRequest.getTelephoneNumber().trim().isEmpty()) {
            metadata.put("telephone_number", signUpRequest.getTelephoneNumber());
        }
        metadata.put("role", signUpRequest.getRole());
        if (signUpRequest.getClinicId() != null) {
            metadata.put("clinic_id", signUpRequest.getClinicId());
        }

        SupabaseAuthClient.SupabaseAuthResponse authResponse;
        try {
            authResponse = supabaseAuthClient.signUp(
                signUpRequest.getEmail(),
                signUpRequest.getPassword(),
                metadata
            );
        } catch (Exception e) {
            log.error("Failed to create user in Supabase Auth", e);
            throw new RuntimeException("Failed to create user account: " + e.getMessage());
        }

        String supabaseUid = authResponse.getUser().getId();
        String accessToken = authResponse.getAccessToken();
        String refreshToken = authResponse.getRefreshToken();
        log.info("Created Supabase Auth user with ID: {}", supabaseUid);

        // Wait briefly for trigger to create User_Profile
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify User_Profile was created by trigger
        UUID userId = UUID.fromString(supabaseUid);
        User baseUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User_Profile was not created by auth trigger"));

        log.info("User_Profile created successfully via trigger for user: {}", supabaseUid);

        // Create role-specific record (Patient, ClinicStaff, or SystemAdministrator)
        UserDto userDto;
        try {
            Role role = Role.valueOf(signUpRequest.getRole().toUpperCase());
            signUpRequest.setId(supabaseUid); // Set the ID from Supabase

            switch (role) {
                case P:
                    userDto = createPatient(signUpRequest);
                    break;
                case C:
                    userDto = createClinicStaff(signUpRequest);
                    break;
                case S:
                    userDto = createSystemAdministrator(signUpRequest);
                    break;
                default:
                    throw new RuntimeException("Invalid role: " + signUpRequest.getRole());
            }

            log.info("Successfully created user with ID: {} and role: {}", supabaseUid, role);

            // Return JwtResponse with tokens and user info using builder
            return JwtResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .id(userDto.getUserId().toString())
                .username(userDto.getEmail())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .role(userDto.getRole().toString())
                .build();

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + signUpRequest.getRole());
        }
    }

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for email: {}", loginRequest.getEmail());

        // Authenticate with Supabase Auth
        SupabaseAuthClient.SupabaseAuthResponse authResponse;
        try {
            authResponse = supabaseAuthClient.signIn(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            );
        } catch (Exception e) {
            log.error("Authentication failed for email: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        String userId = authResponse.getUser().getId();
        String accessToken = authResponse.getAccessToken();
        String refreshToken = authResponse.getRefreshToken();
        log.info("Supabase authentication successful for user: {}", userId);

        // Retrieve user profile from database
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with email: " + loginRequest.getEmail()));

        UserDto userDto = getUserSpecificDto(user);
        log.info("Login successful for user: {}", user.getEmail());

        // Return JwtResponse with tokens and user info using builder
        return JwtResponse.builder()
            .token(accessToken)
            .refreshToken(refreshToken)
            .id(userDto.getUserId().toString())
            .username(userDto.getEmail())
            .email(userDto.getEmail())
            .name(userDto.getName())
            .role(userDto.getRole().toString())
            .build();
    }

    @Override
    public UserDto getCurrentUserProfile(String supabaseUid) {
        log.debug("Fetching profile for user ID: {}", supabaseUid);

        UUID userId = UUID.fromString(supabaseUid);
        User user = userRepository.findById(userId)
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

        UUID userUuid = UUID.fromString(userId);
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with ID: " + userId));

        // Check if new email is already taken
        Optional<User> existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userUuid)) {
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
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with email: " + email));

        // Note: Password reset is handled by Supabase Auth
        // This would typically trigger Supabase's password reset flow

        log.info("Password reset initiated for user: {}", email);
    }

    // Helper methods
    private UserDto createPatient(SignUpRequest request) {
        PatientDto patientDto = PatientDto.builder()
                .userId(UUID.fromString(request.getId()))
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.P)
                .appointmentIds(null)
                .build();

        Patient patient = patientMapper.toEntity(patientDto);
        patient.setRole(Role.P);

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toDto(savedPatient);
    }

    private UserDto createClinicStaff(SignUpRequest request) {
        ClinicStaffDto staffDto = ClinicStaffDto.builder()
                .userId(UUID.fromString(request.getId()))
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.C)
                .clinicId(request.getClinicId())
                .build();

        ClinicStaff clinicStaff = clinicStaffMapper.toEntity(staffDto);
        clinicStaff.setRole(Role.C);

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
                .userId(UUID.fromString(request.getId()))
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.S)
                .build();

        SystemAdministrator admin = systemAdministratorMapper.toEntity(adminDto);
        admin.setRole(Role.S);

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