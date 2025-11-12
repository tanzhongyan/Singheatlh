package Singheatlh.springboot_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.dto.request.RescheduleAppointmentRequest;
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

    /**
     * Cancel an appointment by staff on behalf of a patient.
     * Unlike patient cancellation, this bypasses the 24-hour restriction
     * and records the reason and staff member who performed the cancellation.
     *
     * @param appointmentId The appointment ID to cancel
     * @param staffId The staff user ID performing the cancellation
     * @param reason The reason for cancellation (mandatory)
     * @throws RuntimeException if appointment not found
     * @throws IllegalArgumentException if reason is empty
     */
    void cancelAppointmentByStaff(String appointmentId, UUID staffId, String reason);

    AppointmentDto rescheduleAppointment(String appointmentId, LocalDateTime newDateTime);

    /**
     * Reschedule an appointment with optional doctor and clinic changes.
     * Allows patients to change the date, time, doctor, and/or clinic.
     *
     * @param appointmentId The appointment ID to reschedule
     * @param request The reschedule request containing new date/time and optional doctor/clinic
     * @return The updated appointment DTO
     * @throws RuntimeException if appointment not found
     * @throws IllegalArgumentException if validation fails
     */
    AppointmentDto rescheduleAppointment(String appointmentId, RescheduleAppointmentRequest request);

    /**
     * Reschedule an appointment by staff on behalf of a patient.
     * Unlike patient reschedule, this bypasses the 24-hour restriction
     * and allows same-day rescheduling.
     *
     * @param appointmentId The appointment ID to reschedule
     * @param newDateTime The new appointment date/time
     * @return The updated appointment DTO
     * @throws RuntimeException if appointment not found
     * @throws IllegalArgumentException if validation fails (e.g., doctor unavailable)
     */
    AppointmentDto rescheduleAppointmentByStaff(String appointmentId, LocalDateTime newDateTime);

    /**
     * Reschedule an appointment by staff with optional doctor and clinic changes.
     *
     * @param appointmentId The appointment ID to reschedule
     * @param request The reschedule request containing new date/time and optional doctor/clinic
     * @return The updated appointment DTO
     * @throws RuntimeException if appointment not found
     * @throws IllegalArgumentException if validation fails
     */
    AppointmentDto rescheduleAppointmentByStaff(String appointmentId, RescheduleAppointmentRequest request);

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
