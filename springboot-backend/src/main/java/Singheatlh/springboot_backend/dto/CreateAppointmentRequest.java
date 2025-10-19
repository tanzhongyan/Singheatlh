package Singheatlh.springboot_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {
    
    private UUID patientId;  // Changed to UUID
    
    private String doctorId;  // Changed to String (CHAR(10))
    
    private LocalDateTime startDatetime;  // Renamed from appointmentDatetime
    
    private LocalDateTime endDatetime;  // Added
}
