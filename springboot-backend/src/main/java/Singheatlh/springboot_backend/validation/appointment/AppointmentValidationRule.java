package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;

/**
 * Interface for appointment validation rules.
 * Following Chain of Responsibility pattern and Open/Closed Principle.
 * New validation rules can be added without modifying existing code.
 *
 * This follows the same pattern as ScheduleValidationRule established in the codebase.
 */
public interface AppointmentValidationRule {

    /**
     * Validate an appointment creation request.
     * @param request The appointment request to validate
     * @throws IllegalArgumentException if validation fails with descriptive message
     */
    void validate(CreateAppointmentRequest request);
}
