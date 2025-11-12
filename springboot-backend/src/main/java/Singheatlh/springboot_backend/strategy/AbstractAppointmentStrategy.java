package Singheatlh.springboot_backend.strategy;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.Doctor;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.mapper.AppointmentMapper;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.repository.DoctorRepository;
import Singheatlh.springboot_backend.validation.appointment.AppointmentValidator;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

/**
 * Abstract base class for appointment creation strategies.
 * Implements Template Method pattern to eliminate code duplication.
 * Common workflow is defined here, with strategy-specific behavior delegated to subclasses.
 */
@RequiredArgsConstructor
public abstract class AbstractAppointmentStrategy implements AppointmentCreationStrategy {

    protected final AppointmentRepository appointmentRepository;
    protected final AppointmentMapper appointmentMapper;
    protected final AppointmentValidator appointmentValidator;
    protected final DoctorRepository doctorRepository;

    /**
     * Template method defining the appointment creation workflow.
     * Subclasses can customize behavior through the preprocessRequest hook.
     */
    @Override
    public final AppointmentDto createAppointment(CreateAppointmentRequest request) {
        // Hook method - allow subclasses to modify request before validation
        preprocessRequest(request);

        // Calculate end datetime based on doctor's appointment duration
        calculateEndDatetime(request);

        // Run all validation rules
        appointmentValidator.validate(request);

        // Generate appointment ID
        String appointmentId = generateAppointmentId();

        // Map to entity and set common fields
        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setAppointmentId(appointmentId);
        appointment.setStatus(AppointmentStatus.Upcoming);

        // Persist appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toDto(savedAppointment);
    }

    /**
     * Hook method for subclasses to preprocess the request before validation.
     * Default implementation does nothing.
     *
     * @param request The appointment creation request to preprocess
     */
    protected void preprocessRequest(CreateAppointmentRequest request) {
        // Default: no preprocessing
    }

    /**
     * Calculate the end datetime based on the doctor's appointment duration.
     * Fetches the doctor's appointmentDurationInMinutes and adds it to startDatetime.
     *
     * @param request The appointment creation request to update with calculated endDatetime
     * @throws IllegalArgumentException if doctor not found or duration not configured
     */
    private void calculateEndDatetime(CreateAppointmentRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + request.getDoctorId()));

        if (doctor.getAppointmentDurationInMinutes() == null || doctor.getAppointmentDurationInMinutes() <= 0) {
            throw new IllegalArgumentException("Doctor appointment duration not properly configured");
        }

        // Calculate end datetime by adding duration to start datetime
        LocalDateTime endDateTime = request.getStartDatetime()
            .plusMinutes(doctor.getAppointmentDurationInMinutes());

        request.setEndDatetime(endDateTime);
    }


    /**
     * Generates a unique appointment ID.
     * Format: A000000001, A000000002, etc.
     */
    private String generateAppointmentId() {
        long count = appointmentRepository.count();
        return String.format("A%09d", count + 1);
    }
}
