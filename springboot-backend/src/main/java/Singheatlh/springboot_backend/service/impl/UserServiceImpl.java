package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.UserDto;
import Singheatlh.springboot_backend.entity.User;
import Singheatlh.springboot_backend.entity.enums.Role;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.UserMapper;
import Singheatlh.springboot_backend.repository.UserRepository;
import Singheatlh.springboot_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto getById(String id) {
        UUID userId = UUID.fromString(id);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with id: " + userDto.getUserId()));

        // Update only allowed fields
        user.setName(userDto.getName());
        user.setTelephoneNumber(userDto.getTelephoneNumber());
        // Email and role should be updated through separate auth service

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public void deleteUser(String id) {
        UUID userId = UUID.fromString(id);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundExecption("User not found with id: " + id);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with email: " + email));
        return userMapper.toDto(user);

    }

    @Override
    public int getUserCount() {
        return (int) userRepository.count();
    }

    @Override
    public List<UserDto> getUsersByRole(Role role) {
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

}
