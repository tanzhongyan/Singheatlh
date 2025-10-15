package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.request.LoginRequest;
import Singheatlh.springboot_backend.dto.request.SignUpRequest;
import Singheatlh.springboot_backend.dto.UserDto;

public interface AuthService {
    UserDto signUp(SignUpRequest signUpRequest);
    UserDto login(LoginRequest loginRequest);
    UserDto getCurrentUserProfile(String supabaseUid);
    boolean validateSupabaseJwt(String jwtToken);
    String extractUserIdFromToken(String jwtToken);
    void updateEmail(String userId, String newEmail, String currentPassword);
    void changePassword(String userId, String currentPassword, String newPassword);
    void resetPassword(String email);
}
