package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validates that regular appointments are booked at least one day in advance.
 * Walk-in appointments skip this validation.
 */
@Component
public class MinimumAdvanceBookingValidationRule implements AppointmentValidationRule {

    @Override
    public void validate(CreateAppointmentRequest request) {
        // Skip validation for walk-in appointments
        if (request.isWalkIn()) {
            return;
        }

        // Validate appointment is not for today (must be at least next day)
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);

        if (request.getStartDatetime().isBefore(tomorrow)) {
            throw new IllegalArgumentException(
                "Appointments must be booked at least one day in advance. " +
                "Please select a date from tomorrow onwards."
            );
        }
    }
}
