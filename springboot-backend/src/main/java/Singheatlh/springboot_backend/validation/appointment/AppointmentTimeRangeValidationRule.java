package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.util.TimeRangeValidator;
import org.springframework.stereotype.Component;

/**
 * Validates that end datetime is after start datetime for appointments.
 * Delegates to TimeRangeValidator utility for the actual validation logic.
 */
@Component
public class AppointmentTimeRangeValidationRule implements AppointmentValidationRule {

    @Override
    public void validate(CreateAppointmentRequest request) {
        TimeRangeValidator.validateTimeRangeNonNull(
            request.getStartDatetime(),
            request.getEndDatetime()
        );
    }
}
