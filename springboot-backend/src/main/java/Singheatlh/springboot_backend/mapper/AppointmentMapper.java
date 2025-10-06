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
        dto.setAppointmentId(appointment.getAppointmentId());
        dto.setPatientId(appointment.getPatientId());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setClinicId(appointment.getClinicId());
        dto.setAppointmentDatetime(appointment.getAppointmentDatetime());
        dto.setStatus(appointment.getStatus());
        
        if (appointment.getPatient() != null) {
            dto.setPatientName(appointment.getPatient().getName());
        }
        
        return dto;
    }
    
     
    public Appointment toEntity(CreateAppointmentRequest request) {
        if (request == null) {
            return null;
        }
        
        return new Appointment(
            request.getAppointmentDatetime(),
            request.getDoctorId(),
            request.getClinicId(),
            request.getPatientId()
        );
    }
}
