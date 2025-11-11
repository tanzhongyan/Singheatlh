package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.PaginatedResponse;
import Singheatlh.springboot_backend.dto.UserDto;
import Singheatlh.springboot_backend.entity.enums.Role;

import java.util.List;

public interface UserService {
    UserDto getById(String id);

    List<UserDto> getAllUsers();

    UserDto updateUser(UserDto userDto);

    void deleteUser(String id);

    UserDto getByEmail(String email);

    int getUserCount();

    List<UserDto> getUsersByRole(Role role);

    PaginatedResponse<UserDto> getUsersWithPagination(int page, int pageSize, String search, String role);
}
