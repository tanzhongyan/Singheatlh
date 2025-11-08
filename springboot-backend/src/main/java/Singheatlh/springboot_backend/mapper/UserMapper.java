package Singheatlh.springboot_backend.mapper;

import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.dto.UserDto;
import Singheatlh.springboot_backend.entity.User;
import Singheatlh.springboot_backend.entity.enums.Role;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class UserMapper {

    // Strategy Pattern: Map of role-specific mappers
    private final Map<Role, Function<User, UserDto>> mapperStrategies;

    public UserMapper() {
        mapperStrategies = new HashMap<>();

        // Register role-specific mapping strategies
        mapperStrategies.put(Role.C, this::toClinicStaffDto);  // Clinic staff
        mapperStrategies.put(Role.S, this::toClinicStaffDto);  // Doctors (also have clinicId)
        mapperStrategies.put(Role.P, this::toBaseDto);         // Patients (no clinicId)
    }

    /**
     * Converts User entity to appropriate DTO based on role using Strategy Pattern.
     * Returns ClinicStaffDto for clinic staff and doctors (includes clinicId),
     * base UserDto for patients (no clinicId needed).
     */
    public UserDto toDto(User user) {
        if (user == null) return null;

        // Use strategy pattern - lookup and execute the appropriate mapper
        Function<User, UserDto> mapper = mapperStrategies.getOrDefault(user.getRole(), this::toBaseDto);
        return mapper.apply(user);
    }

    /**
     * Maps to ClinicStaffDto for clinic staff and doctors (both have clinicId)
     */
    private ClinicStaffDto toClinicStaffDto(User user) {
        return ClinicStaffDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .telephoneNumber(user.getTelephoneNumber())
                .clinicId(user.getClinicId())
                .build();
    }

    /**
     * Maps to base UserDto for patients (no clinicId needed)
     */
    private UserDto toBaseDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .telephoneNumber(user.getTelephoneNumber())
                .build();
    }

    public User toEntity(UserDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUserId(dto.getUserId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setTelephoneNumber(dto.getTelephoneNumber());

        // Handle clinicId if it's a ClinicStaffDto
        if (dto instanceof ClinicStaffDto) {
            user.setClinicId(((ClinicStaffDto) dto).getClinicId());
        }

        return user;
    }
}
