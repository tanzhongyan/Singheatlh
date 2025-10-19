package Singheatlh.springboot_backend.mapper;

import Singheatlh.springboot_backend.entity.Appointment;
import org.springframework.stereotype.Component;
import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.PatientDto;
import Singheatlh.springboot_backend.entity.Patient;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PatientMapper {


    public PatientDto toDto(Patient patient) {
        if (patient == null) return null;

        PatientDto dto = new PatientDto();
        dto.setUserId(patient.getUserId());
        dto.setName(patient.getName());
        dto.setEmail(patient.getEmail());
        dto.setRole(patient.getRole());
        dto.setTelephoneNumber(patient.getTelephoneNumber());
        // Patient doesn't have clinicId in DTO (only ClinicStaff does)

        if (patient.getAppointments() != null) {
            List<String> appointmentIds = patient.getAppointments().stream()
                    .map(Appointment::getAppointmentId)
                    .collect(Collectors.toList());
            dto.setAppointmentIds(appointmentIds);
        }

        return dto;
    }

    public Patient toEntity(PatientDto dto) {
        if (dto == null) return null;

        Patient patient = new Patient();
        patient.setUserId(dto.getUserId());
        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setRole(dto.getRole());
        patient.setTelephoneNumber(dto.getTelephoneNumber());
        // Patient doesn't have clinicId in DTO (only ClinicStaff does)
        // appointments handled by AppointmentService or separate mapper logic
        return patient;
    }
}
