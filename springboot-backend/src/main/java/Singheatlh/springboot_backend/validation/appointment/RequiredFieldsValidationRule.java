package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import org.springframework.stereotype.Component;

/**
 * Validates that all required fields are present in the appointment request.
 */
@Component
public class RequiredFieldsValidationRule implements AppointmentValidationRule {

    @Override
    public void validate(CreateAppointmentRequest request) {
        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("Patient ID must not be null");
        }

        if (request.getDoctorId() == null || request.getDoctorId().trim().isEmpty()) {
            throw new IllegalArgumentException("Doctor ID must not be null or empty");
        }

        if (request.getStartDatetime() == null) {
            throw new IllegalArgumentException("Start datetime must not be null");
        }

        if (request.getEndDatetime() == null) {
            throw new IllegalArgumentException("End datetime must not be null");
        }
    }
}
