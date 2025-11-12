package Singheatlh.springboot_backend.strategy;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.mapper.AppointmentMapper;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.repository.DoctorRepository;
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
                                    AppointmentValidator appointmentValidator,
                                    DoctorRepository doctorRepository) {
        super(appointmentRepository, appointmentMapper, appointmentValidator, doctorRepository);
    }

    @Override
    public String getStrategyName() {
        return "WALK_IN";
    }

    /**
     * No preprocessing needed for walk-in appointments.
     * The isWalkIn flag is already set by the controller/frontend and verified by AppointmentStrategyFactory.
     * All validation rules check this flag and skip their checks for walk-ins.
     */
    @Override
    protected void preprocessRequest(CreateAppointmentRequest request) {
        // Walk-in flag is already set; no preprocessing needed
    }
}
