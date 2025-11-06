package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

/**
 * Validates that end datetime is after start datetime.
 */
@Component
public class TimeRangeValidationRule implements AppointmentValidationRule {

    @Override
    public void validate(CreateAppointmentRequest request) {
        if (request.getEndDatetime().isBefore(request.getStartDatetime()) ||
            request.getEndDatetime().isEqual(request.getStartDatetime())) {
            throw new IllegalArgumentException("End datetime must be after start datetime");
        }
    }
}
