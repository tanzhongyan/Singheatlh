package Singheatlh.springboot_backend.dto;

import java.time.LocalDateTime;

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
    
    private Long appointmentId;
    private Long patientId;
    private String patientName; 
    private Long doctorId;
    private Long clinicId;
    private LocalDateTime appointmentDatetime;
    private AppointmentStatus status;
}
