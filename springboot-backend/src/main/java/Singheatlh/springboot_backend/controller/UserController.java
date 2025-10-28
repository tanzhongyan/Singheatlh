package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.dto.UserDto;
import Singheatlh.springboot_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        UserDto user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        UserDto user = userService.getByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String id,
            @RequestBody UserDto userDto) {
        userDto.setUserId(UUID.fromString(id)); // Ensure path ID is used
        UserDto updatedUser = userService.updateUser(userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getUserCount() {
        int count = userService.getUserCount();
        return ResponseEntity.ok(count);
    }

}
