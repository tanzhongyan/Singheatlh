package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Orchestrates all appointment validation rules.
 * Following Chain of Responsibility pattern.
 * New rules can be added by creating new AppointmentValidationRule implementations.
 */
@Component
@RequiredArgsConstructor
public class AppointmentValidator {

    private final List<AppointmentValidationRule> validationRules;

    /**
     * Validate an appointment request against all registered rules.
     * @param request The appointment request to validate
     * @throws IllegalArgumentException if any validation rule fails
     */
    public void validate(CreateAppointmentRequest request) {
        validationRules.forEach(rule -> rule.validate(request));
    }
}
