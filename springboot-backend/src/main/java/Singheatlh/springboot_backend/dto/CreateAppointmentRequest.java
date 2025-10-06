package Singheatlh.springboot_backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {
    
    private Long patientId;
    
    private Long doctorId;
    
    private Long clinicId;
    
    private LocalDateTime appointmentDatetime;
}
