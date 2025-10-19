package Singheatlh.springboot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import Singheatlh.springboot_backend.entity.enums.Role;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserDto {
    private UUID userId;
    private String name;
    private String email;
    private Role role;  // Use enum for type safety (P, C, S)
    private String telephoneNumber;
}
