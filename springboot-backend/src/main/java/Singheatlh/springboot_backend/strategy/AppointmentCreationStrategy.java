package Singheatlh.springboot_backend.strategy;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;

/**
 * Strategy interface for different appointment creation types.
 * Following Strategy Pattern and Open/Closed Principle.
 * New appointment types can be added by implementing this interface.
 */
public interface AppointmentCreationStrategy {

    /**
     * Get the name of this strategy for identification.
     * @return Strategy name (e.g., "REGULAR", "WALK_IN")
     */
    String getStrategyName();

    /**
     * Create an appointment using this strategy's specific logic.
     * @param request The appointment creation request
     * @return The created appointment DTO
     * @throws IllegalArgumentException if creation fails validation
     */
    AppointmentDto createAppointment(CreateAppointmentRequest request);
}
