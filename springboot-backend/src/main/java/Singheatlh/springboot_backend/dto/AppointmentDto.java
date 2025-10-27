package Singheatlh.springboot_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    
    private String appointmentId;  // CHAR(10)
    private UUID patientId;  // UUID
    private String patientName; 
    private String doctorId;  // CHAR(10)
    private String doctorName;  // Doctor's name for display
    private String clinicName;  // Clinic name for display
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private AppointmentStatus status;  // Changed to enum for type safety
}
