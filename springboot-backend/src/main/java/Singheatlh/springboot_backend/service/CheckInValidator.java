package Singheatlh.springboot_backend.service;

import java.time.LocalDateTime;

import Singheatlh.springboot_backend.entity.Appointment;

/**
 * Validator service responsible for validating check-in operations
 * Separates validation logic from business logic (SRP)
 */
public interface CheckInValidator {
    
    /**
     * Validate that check-in can proceed
     * @param appointment The appointment to validate
     * @param now Current time
     * @throws IllegalStateException if validation fails
     */
    void validateCheckIn(Appointment appointment, LocalDateTime now);
    
    /**
     * Validate appointment date is not in the past
     * @param appointment The appointment to validate
     * @param now Current time
     * @throws IllegalStateException if date is in the past
     */
    void validateAppointmentDate(Appointment appointment, LocalDateTime now);
    
    /**
     * Validate appointment status allows check-in
     * @param appointment The appointment to validate
     * @throws IllegalStateException if status doesn't allow check-in
     */
    void validateAppointmentStatus(Appointment appointment);
    
    /**
     * Validate no duplicate check-in exists
     * @param appointmentId The appointment ID to check
     * @throws IllegalStateException if duplicate check-in exists
     */
    void validateNoDuplicateCheckIn(String appointmentId);
}

