package Singheatlh.springboot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicDto {
    private Integer clinicId;
    private String name;
    private String address;
    private String telephoneNumber;
    private String type;
    private LocalTime openingHours;
    private LocalTime closingHours;
}
