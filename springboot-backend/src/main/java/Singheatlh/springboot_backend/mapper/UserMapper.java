package Singheatlh.springboot_backend.mapper;

import Singheatlh.springboot_backend.dto.UserDto;
import Singheatlh.springboot_backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setTelephoneNumber(user.getTelephoneNumber());
        // clinicId not included in base UserDto - only in ClinicStaffDto
        return dto;
    }

    public User toEntity(UserDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUserId(dto.getUserId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setTelephoneNumber(dto.getTelephoneNumber());
        // clinicId not included in base UserDto - only in ClinicStaffDto
        return user;
    }
}
