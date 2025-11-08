package Singheatlh.springboot_backend.strategy;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.mapper.AppointmentMapper;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.validation.appointment.AppointmentValidator;
import org.springframework.stereotype.Component;

/**
 * Strategy for creating walk-in appointments.
 * Bypasses future time validation to allow immediate appointment creation.
 * Staff can create appointments for patients who walk in without prior booking.
 *
 * Uses Template Method pattern via AbstractAppointmentStrategy.
 * Overrides preprocessRequest() to set the isWalkIn flag.
 */
@Component
public class WalkInAppointmentStrategy extends AbstractAppointmentStrategy {

    public WalkInAppointmentStrategy(AppointmentRepository appointmentRepository,
                                    AppointmentMapper appointmentMapper,
                                    AppointmentValidator appointmentValidator) {
        super(appointmentRepository, appointmentMapper, appointmentValidator);
    }

    @Override
    public String getStrategyName() {
        return "WALK_IN";
    }

    /**
     * Sets the isWalkIn flag to true before validation.
     * This allows FutureTimeValidationRule to skip the future time check.
     */
    @Override
    protected void preprocessRequest(CreateAppointmentRequest request) {
        request.setIsWalkIn(true);
    }
}
