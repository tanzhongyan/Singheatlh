package Singheatlh.springboot_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String refreshToken;
    private String id;
    private String username;
    private String email;
    private String name;
    private String role;

    public JwtResponse(String token, String id, String username, String email, String name, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public JwtResponse(String token, String refreshToken, String id, String username, String email, String name, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}