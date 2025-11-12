package Singheatlh.springboot_backend.strategy;

import Singheatlh.springboot_backend.mapper.AppointmentMapper;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.repository.DoctorRepository;
import Singheatlh.springboot_backend.validation.appointment.AppointmentValidator;
import org.springframework.stereotype.Component;

/**
 * Strategy for creating regular (non-walk-in) appointments.
 * Applies all standard validations including future time requirements.
 *
 * Uses Template Method pattern via AbstractAppointmentStrategy.
 * No preprocessing needed - uses default behavior from base class.
 */
@Component
public class RegularAppointmentStrategy extends AbstractAppointmentStrategy {

    public RegularAppointmentStrategy(AppointmentRepository appointmentRepository,
                                     AppointmentMapper appointmentMapper,
                                     AppointmentValidator appointmentValidator,
                                     DoctorRepository doctorRepository) {
        super(appointmentRepository, appointmentMapper, appointmentValidator, doctorRepository);
    }

    @Override
    public String getStrategyName() {
        return "REGULAR";
    }

    // Uses default preprocessRequest() from AbstractAppointmentStrategy
    // No custom preprocessing needed for regular appointments
}
