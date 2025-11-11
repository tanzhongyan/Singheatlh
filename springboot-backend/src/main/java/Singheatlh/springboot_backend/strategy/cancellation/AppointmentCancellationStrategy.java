package Singheatlh.springboot_backend.strategy.cancellation;

import Singheatlh.springboot_backend.entity.Appointment;

/**
 * Strategy interface for different appointment cancellation types.
 * Following Strategy Pattern and Open/Closed Principle.
 * New cancellation rules can be added by implementing this interface.
 */
public interface AppointmentCancellationStrategy {

    /**
     * Get the name of this strategy for identification.
     * @return Strategy name (e.g., "PATIENT", "STAFF")
     */
    String getStrategyName();

    /**
     * Cancel an appointment using this strategy's specific rules.
     * @param appointment The appointment to cancel
     * @param context The cancellation context (who is canceling, reason, etc.)
     * @throws IllegalArgumentException if cancellation fails validation
     * @throws IllegalStateException if appointment cannot be cancelled
     */
    void cancel(Appointment appointment, CancellationContext context);
}
