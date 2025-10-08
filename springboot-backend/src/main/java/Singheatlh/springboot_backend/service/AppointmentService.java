package Singheatlh.springboot_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;

public interface AppointmentService {
    
    
    AppointmentDto createAppointment(CreateAppointmentRequest request);
    
    
    AppointmentDto getAppointmentById(Long appointmentId);
    
    
    List<AppointmentDto> getAppointmentsByPatientId(Long patientId);
    
    
    List<AppointmentDto> getAppointmentsByDoctorId(Long doctorId);
    
    
    List<AppointmentDto> getAppointmentsByClinicId(Long clinicId);
    
    
    List<AppointmentDto> getUpcomingAppointmentsByPatientId(Long patientId);
    
    
    List<AppointmentDto> getUpcomingAppointmentsByDoctorId(Long doctorId);
    
    
    AppointmentDto updateAppointmentStatus(Long appointmentId, AppointmentStatus status);
    
    
    void cancelAppointment(Long appointmentId);
    
    
    AppointmentDto rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime);
    
    
    List<AppointmentDto> getAllAppointments();
    
    
    List<AppointmentDto> getAppointmentsByStatus(AppointmentStatus status);
}
