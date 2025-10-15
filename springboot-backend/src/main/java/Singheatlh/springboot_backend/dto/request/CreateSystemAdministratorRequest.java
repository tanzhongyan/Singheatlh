package Singheatlh.springboot_backend.dto.request;

import lombok.Data;

@Data
public class CreateSystemAdministratorRequest {
    private String id;
    private String username;
    private String name;
    private String email;
}
