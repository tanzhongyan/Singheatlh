package Singheatlh.springboot_backend.strategy;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.mapper.AppointmentMapper;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.validation.appointment.AppointmentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Strategy for creating regular (non-walk-in) appointments.
 * Applies all standard validations including future time requirements.
 */
@Component
@RequiredArgsConstructor
public class RegularAppointmentStrategy implements AppointmentCreationStrategy {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentValidator appointmentValidator;

    @Override
    public String getStrategyName() {
        return "REGULAR";
    }

    @Override
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        // Run all validation rules (including future time check)
        appointmentValidator.validate(request);

        // Generate appointment ID
        String appointmentId = generateAppointmentId();

        // Map to entity
        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setAppointmentId(appointmentId);
        appointment.setStatus(AppointmentStatus.Upcoming);

        // Persist
        Appointment savedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toDto(savedAppointment);
    }

    private String generateAppointmentId() {
        long count = appointmentRepository.count();
        return String.format("A%09d", count + 1);
    }
}
