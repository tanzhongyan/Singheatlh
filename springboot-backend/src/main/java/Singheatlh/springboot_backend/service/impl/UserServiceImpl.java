package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.UserDto;
import Singheatlh.springboot_backend.entity.User;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.UserMapper;
import Singheatlh.springboot_backend.repository.UserRepository;
import Singheatlh.springboot_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Override
    public UserDto getById(String id) {
        User user = userRepository.findById(id)
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
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new ResourceNotFoundExecption("User not found with id: " + userDto.getId()));

        // Update only allowed fields
        user.setName(userDto.getName());
        user.setUsername(userDto.getUsername());
        // Email and role should be updated through separate auth service

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundExecption("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDto getByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundExecption("User not found with email: " + email));
        return userMapper.toDto(user);

    }

}
