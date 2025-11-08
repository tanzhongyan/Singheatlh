package Singheatlh.springboot_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;

public interface AppointmentService {
    
    AppointmentDto createAppointment(CreateAppointmentRequest request);

    /**
     * Create a walk-in appointment (bypasses future time validation)
     * @param request The appointment creation request
     * @return The created appointment DTO
     */
    AppointmentDto createWalkInAppointment(CreateAppointmentRequest request);


    AppointmentDto getAppointmentById(String appointmentId);
    
    
    List<AppointmentDto> getAppointmentsByPatientId(UUID patientId);
    
    List<AppointmentDto> getUpcomingAppointmentsByPatientId(UUID patientId);
    
    void cancelAppointment(String appointmentId);
    
    AppointmentDto rescheduleAppointment(String appointmentId, LocalDateTime newDateTime);

    // ========== Clinic Staff Methods ==========

    /**
     * Get all appointments for a specific clinic
     * @param clinicId The clinic ID
     * @return List of all appointments for the clinic
     */
    List<AppointmentDto> getAppointmentsByClinicId(Integer clinicId);

    /**
     * Get appointments for a clinic filtered by status
     * @param clinicId The clinic ID
     * @param status The appointment status
     * @return List of appointments matching the criteria
     */
    List<AppointmentDto> getAppointmentsByClinicIdAndStatus(Integer clinicId, AppointmentStatus status);

    /**
     * Get today's appointments for a specific clinic
     * @param clinicId The clinic ID
     * @return List of today's appointments for the clinic
     */
    List<AppointmentDto> getTodayAppointmentsByClinicId(Integer clinicId);

    /**
     * Get appointments for a clinic within a date range
     * @param clinicId The clinic ID
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of appointments in the date range
     */
    List<AppointmentDto> getAppointmentsByClinicIdAndDateRange(
        Integer clinicId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get upcoming appointments for a specific clinic
     * @param clinicId The clinic ID
     * @return List of upcoming appointments for the clinic
     */
    List<AppointmentDto> getUpcomingAppointmentsByClinicId(Integer clinicId);
}
