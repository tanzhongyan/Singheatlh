package Singheatlh.springboot_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;

public interface AppointmentService {
    
    
    AppointmentDto createAppointment(CreateAppointmentRequest request);
    
    
    AppointmentDto getAppointmentById(String appointmentId);
    
    
    List<AppointmentDto> getAppointmentsByPatientId(UUID patientId);
    
    
    List<AppointmentDto> getAppointmentsByDoctorId(String doctorId);
    
    
    List<AppointmentDto> getUpcomingAppointmentsByPatientId(UUID patientId);
    
    
    List<AppointmentDto> getUpcomingAppointmentsByDoctorId(String doctorId);
    
    
    AppointmentDto updateAppointmentStatus(String appointmentId, AppointmentStatus status);
    
    
    void cancelAppointment(String appointmentId);
    
    
    AppointmentDto rescheduleAppointment(String appointmentId, LocalDateTime newDateTime);
    
    
    List<AppointmentDto> getAllAppointments();
    
    
    List<AppointmentDto> getAppointmentsByStatus(AppointmentStatus status);
}
