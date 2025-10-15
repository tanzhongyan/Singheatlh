package Singheatlh.springboot_backend.dto.request;

import lombok.Data;

@Data
public class UpdateClinicHoursRequest {
    private String openingHours;
    private String closingHours;
}
