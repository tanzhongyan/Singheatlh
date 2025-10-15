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
        dto.setId(patient.getSupabaseUid());
        dto.setUsername(patient.getUsername());
        dto.setName(patient.getName());
        dto.setEmail(patient.getEmail());
        dto.setRole(patient.getRole());

        if (patient.getAppointments() != null) {
            List<Long> appointmentIds = patient.getAppointments().stream()
                    .map(Appointment::getAppointmentId)
                    .collect(Collectors.toList());
            dto.setAppointmentIds(appointmentIds);
        }

        return dto;
    }

    public Patient toEntity(PatientDto dto) {
        if (dto == null) return null;

        Patient patient = new Patient();
        patient.setSupabaseUid(dto.getId());
        patient.setUsername(dto.getUsername());
        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setRole(dto.getRole());
        // appointments handled by AppointmentService or separate mapper logic
        return patient;
    }
}
