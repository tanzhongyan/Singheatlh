package Singheatlh.springboot_backend.strategy.cancellation;

import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;

/**
 * Abstract base class for appointment cancellation strategies.
 * Implements Template Method pattern to eliminate code duplication.
 * Common workflow is defined here, with strategy-specific validation delegated to subclasses.
 */
@RequiredArgsConstructor
public abstract class AbstractCancellationStrategy implements AppointmentCancellationStrategy {

    protected final AppointmentRepository appointmentRepository;

    /**
     * Template method defining the cancellation workflow.
     * Subclasses customize behavior through the validateCancellation hook.
     */
    @Override
    public final void cancel(Appointment appointment, CancellationContext context) {
        // Hook method - allow subclasses to add specific validation
        validateCancellation(appointment, context);

        // Common logic - update appointment status
        updateAppointmentStatus(appointment);

        // Common logic - record cancellation metadata
        recordCancellationMetadata(appointment, context);

        // Persist changes
        appointmentRepository.save(appointment);
    }

    /**
     * Hook method for subclasses to implement strategy-specific validation.
     * Each strategy (Patient, Staff) has different cancellation rules.
     *
     * @param appointment The appointment to validate
     * @param context The cancellation context
     * @throws IllegalArgumentException if cancellation fails validation
     * @throws IllegalStateException if appointment cannot be cancelled
     */
    protected abstract void validateCancellation(Appointment appointment, CancellationContext context);

    /**
     * Update the appointment status to Cancelled.
     * Common logic shared by all cancellation strategies.
     */
    private void updateAppointmentStatus(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.Cancelled);
    }

    /**
     * Record cancellation metadata (who, when, why).
     * Common logic shared by all cancellation strategies.
     */
    private void recordCancellationMetadata(Appointment appointment, CancellationContext context) {
        appointment.setCancelledBy(context.getCancelledBy());
        appointment.setCancelledAt(context.getNow());

        // Set reason if provided (mandatory for staff, optional for patients)
        if (context.getReason() != null && !context.getReason().trim().isEmpty()) {
            appointment.setCancellationReason(context.getReason());
        }
    }
}
