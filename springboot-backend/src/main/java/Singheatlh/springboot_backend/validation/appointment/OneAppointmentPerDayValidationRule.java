package Singheatlh.springboot_backend.validation.appointment;

import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates that a patient can only have one appointment per day.
 * Only checks active appointments (Upcoming or Ongoing status).
 * Skips validation for walk-in appointments.
 */
@Component
@RequiredArgsConstructor
public class OneAppointmentPerDayValidationRule implements AppointmentValidationRule {

    private final AppointmentRepository appointmentRepository;

    @Override
    public void validate(CreateAppointmentRequest request) {
        // Skip validation for walk-in appointments
        if (request.isWalkIn()) {
            return;
        }

        // Check if patient already has an appointment on the same day
        LocalDateTime startOfDay = request.getStartDatetime().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        List<Appointment> patientAppointmentsOnDay = appointmentRepository
            .findByPatientIdAndStartDatetimeBetween(request.getPatientId(), startOfDay, endOfDay)
            .stream()
            .filter(apt -> apt.getStatus() == AppointmentStatus.Upcoming ||
                          apt.getStatus() == AppointmentStatus.Ongoing)
            .collect(Collectors.toList());

        if (!patientAppointmentsOnDay.isEmpty()) {
            throw new IllegalArgumentException(
                "You already have an appointment scheduled on this day. " +
                "Please choose a different date."
            );
        }
    }
}
