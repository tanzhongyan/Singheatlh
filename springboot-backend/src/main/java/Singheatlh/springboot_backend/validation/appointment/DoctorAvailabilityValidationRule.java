package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates that the doctor is available during the requested time slot.
 * Checks for conflicting appointments in the database.
 */
@Component
@RequiredArgsConstructor
public class DoctorAvailabilityValidationRule implements AppointmentValidationRule {

    private final AppointmentRepository appointmentRepository;

    @Override
    public void validate(CreateAppointmentRequest request) {
        List<Appointment> conflictingAppointments = appointmentRepository
                .findByDoctorIdAndStartDatetimeBetween(
                        request.getDoctorId(),
                        request.getStartDatetime(),
                        request.getEndDatetime()
                );

        if (!conflictingAppointments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Doctor is not available during the requested time slot"
            );
        }
    }
}
