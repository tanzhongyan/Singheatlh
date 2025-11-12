package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates that the doctor is available during the requested time slot.
 * Checks for conflicting appointments in the database.
 * Only considers active appointments (Upcoming or Ongoing status).
 * Includes a 30-minute buffer before the appointment time.
 */
@Component
@RequiredArgsConstructor
public class DoctorAvailabilityValidationRule implements AppointmentValidationRule {

    private final AppointmentRepository appointmentRepository;

    @Override
    public void validate(CreateAppointmentRequest request) {
        // Check for conflicting appointments (actual time overlap only)
        // No buffer - appointments can be back-to-back as long as they don't overlap
        List<Appointment> conflictingAppointments = appointmentRepository
                .findByDoctorIdAndStartDatetimeBetween(
                        request.getDoctorId(),
                        request.getStartDatetime(),
                        request.getEndDatetime()
                )
                .stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.Upcoming ||
                              apt.getStatus() == AppointmentStatus.Ongoing)
                .filter(apt -> {
                    // Check for actual time overlap
                    // Conflict if: apt.start < new.end AND apt.end > new.start
                    return apt.getStartDatetime().isBefore(request.getEndDatetime()) &&
                           apt.getEndDatetime().isAfter(request.getStartDatetime());
                })
                .collect(Collectors.toList());

        if (!conflictingAppointments.isEmpty()) {
            throw new IllegalArgumentException("Doctor is not available at the requested time");
        }
    }
}
