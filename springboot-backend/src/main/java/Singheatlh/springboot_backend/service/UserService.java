package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getById(String id);
    List<UserDto> getAllUsers();
    UserDto updateUser(UserDto userDto);
    void deleteUser(String id);
    UserDto getByEmail(String email);
}
