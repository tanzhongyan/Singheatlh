package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.validation.ValidationRule;

/**
 * Interface for appointment validation rules.
 * Following Chain of Responsibility pattern and Open/Closed Principle.
 * New validation rules can be added without modifying existing code.
 *
 * Extends the generic ValidationRule interface for type safety and consistency.
 */
public interface AppointmentValidationRule extends ValidationRule<CreateAppointmentRequest> {
    // Inherits: void validate(CreateAppointmentRequest request);
}
