package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.PatientDto;
import Singheatlh.springboot_backend.dto.PatientDto;

import java.util.List;

public interface PatientService {
    PatientDto getById(String id);
    PatientDto createPatient(PatientDto patientDto);
    List<PatientDto> getAllPatients();
    PatientDto updatePatient(PatientDto patientDto);
    void deletePatient(String id);
    PatientDto getByEmail(String email);
}
