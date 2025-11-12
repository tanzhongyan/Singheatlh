package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validates that appointment is scheduled in the future.
 * Skips validation for walk-in appointments to allow immediate scheduling.
 */
@Component
public class FutureTimeValidationRule implements AppointmentValidationRule {

    @Override
    public void validate(CreateAppointmentRequest request) {
        // Skip validation for walk-in appointments
        if (request.isWalkIn()) {
            return;
        }

        // For regular appointments, ensure they're scheduled in the future
        if (request.getStartDatetime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment cannot be scheduled in the past");
        }
    }
}
