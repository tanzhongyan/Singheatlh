package Singheatlh.springboot_backend.strategy.cancellation;

import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Strategy for patient-initiated appointment cancellations.
 * Enforces patient-specific cancellation rules:
 * - Cannot cancel past appointments
 * - Must cancel at least 24 hours in advance
 * - Cancellation reason is optional
 */
@Component
public class PatientCancellationStrategy extends AbstractCancellationStrategy {

    public PatientCancellationStrategy(AppointmentRepository appointmentRepository) {
        super(appointmentRepository);
    }

    @Override
    public String getStrategyName() {
        return "PATIENT";
    }

    @Override
    protected void validateCancellation(Appointment appointment, CancellationContext context) {
        LocalDateTime appointmentTime = appointment.getStartDatetime();
        LocalDateTime now = context.getNow();

        // Rule 1: Cannot cancel past appointments
        if (appointmentTime.isBefore(now)) {
            throw new IllegalStateException("Cannot cancel past appointments");
        }

        // Rule 2: Must cancel at least 24 hours in advance
        if (appointmentTime.isBefore(now.plusHours(24))) {
            throw new IllegalArgumentException(
                "Cannot cancel appointments less than 24 hours in advance"
            );
        }

        // Note: Cancellation reason is optional for patients
        // No validation required for reason field
    }
}
