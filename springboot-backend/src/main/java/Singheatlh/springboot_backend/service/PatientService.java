package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.PatientDto;
import Singheatlh.springboot_backend.dto.PatientDto;

import java.util.List;

public interface PatientService {
    PatientDto getById(Long id);
    PatientDto createPatient(PatientDto patientDto, String password);
    List<PatientDto> getAllPatients();
    PatientDto updatePatient(PatientDto patientDto);
    void deletePatient(Long id);
}
