package Singheatlh.springboot_backend.strategy.cancellation;

import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

/**
 * Strategy for staff-initiated appointment cancellations.
 * Enforces staff-specific cancellation rules:
 * - Can cancel at any time (no 24-hour restriction)
 * - Can cancel past appointments (for administrative cleanup)
 * - Cancellation reason is mandatory for accountability
 */
@Component
public class StaffCancellationStrategy extends AbstractCancellationStrategy {

    public StaffCancellationStrategy(AppointmentRepository appointmentRepository) {
        super(appointmentRepository);
    }

    @Override
    public String getStrategyName() {
        return "STAFF";
    }

    @Override
    protected void validateCancellation(Appointment appointment, CancellationContext context) {
        // Rule 1: Reason is mandatory for staff cancellations (accountability)
        if (context.getReason() == null || context.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Cancellation reason is required for staff cancellations"
            );
        }

        // Rule 2: No time restrictions
        // Staff can cancel appointments at any time, including:
        // - Less than 24 hours before appointment
        // - Same day as appointment
        // - Past appointments (for administrative purposes)
        // This flexibility is intentional for staff operations
    }
}
