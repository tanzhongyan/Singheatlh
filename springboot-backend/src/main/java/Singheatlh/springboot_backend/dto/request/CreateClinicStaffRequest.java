package Singheatlh.springboot_backend.dto.request;

import lombok.Data;

@Data
public class CreateClinicStaffRequest {
    private String id;
    private String email;
    private String password;
    private String name;
    private String username;
    private int clinicId;
}
