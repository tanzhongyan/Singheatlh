package Singheatlh.springboot_backend.mapper;

import org.springframework.stereotype.Component;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.Appointment;

@Component
public class AppointmentMapper {
    
    public AppointmentDto toDto(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        
        AppointmentDto dto = new AppointmentDto();
        dto.setAppointmentId(appointment.getAppointmentId());  // String
        dto.setPatientId(appointment.getPatientId());  // UUID
        dto.setDoctorId(appointment.getDoctorId());  // String
        dto.setStartDatetime(appointment.getStartDatetime());
        dto.setEndDatetime(appointment.getEndDatetime());
        dto.setStatus(appointment.getStatus());  // String
        
        // Set patient name if patient relationship is loaded
        if (appointment.getPatient() != null) {
            dto.setPatientName(appointment.getPatient().getName());
        }
        
        if (appointment.getDoctor() != null) {
            dto.setDoctorName(appointment.getDoctor().getName());
            
            if (appointment.getDoctor().getClinic() != null) {
                dto.setClinicName(appointment.getDoctor().getClinic().getName());
            }
        }
        
        return dto;
    }
    
     
    public Appointment toEntity(CreateAppointmentRequest request) {
        if (request == null) {
            return null;
        }
        
        return new Appointment(
            null,  
            request.getPatientId(),  // UUID
            request.getDoctorId(),  // String
            request.getStartDatetime(),
            request.getEndDatetime()
        );
    }
}
