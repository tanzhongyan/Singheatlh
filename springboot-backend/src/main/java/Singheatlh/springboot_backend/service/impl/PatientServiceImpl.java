package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.PatientDto;
import Singheatlh.springboot_backend.entity.Patient;
import Singheatlh.springboot_backend.entity.enums.Role;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.PatientMapper;
import Singheatlh.springboot_backend.repository.PatientRepository;
import Singheatlh.springboot_backend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Override
    public PatientDto getById(String id) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundExecption("Patient does not exist with the given id " + id)
        );
        return patientMapper.toDto(patient);
    }

    @Override
    public PatientDto createPatient(PatientDto patientDto) {
        if (patientRepository.existsById(patientDto.getId())) {
            throw new RuntimeException("Patient already exists with id: " + patientDto.getId());
        }

        Patient patient = patientMapper.toEntity(patientDto);
        patient.setRole(Role.PATIENT);
        // ✅ No password handling - handled by Supabase Auth

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toDto(savedPatient);
    }

    @Override
    public List<PatientDto> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PatientDto updatePatient(PatientDto patientDto) {
        Patient patient = patientRepository.findById(patientDto.getId()).orElseThrow(
                () -> new ResourceNotFoundExecption("Patient does not exist with the given id " + patientDto.getId())
        );

        // ✅ Update fields
        patient.setName(patientDto.getName());
        patient.setUsername(patientDto.getUsername());

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toDto(savedPatient);
    }

    @Override
    public void deletePatient(String id) {
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundExecption("Patient does not exist with the given id " + id)
        );
        patientRepository.deleteById(id);
    }

    @Override
    public PatientDto getByEmail(String email) {
        Patient patient = patientRepository.findByEmail(email).orElseThrow(
                ()-> new ResourceNotFoundExecption("Patient does not exist with the given email: " + email)
        );
        return patientMapper.toDto(patient);
    }
}
