package Singheatlh.springboot_backend.service.impl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Singheatlh.springboot_backend.dto.ClinicDto;
import Singheatlh.springboot_backend.entity.Clinic;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.ClinicMapper;
import Singheatlh.springboot_backend.repository.ClinicRepository;
import Singheatlh.springboot_backend.service.ClinicManagementService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ClinicManagementServiceImpl implements ClinicManagementService {

    private ClinicRepository clinicRepository;
    private ClinicMapper clinicMapper;

    @Override
    @Transactional
    public ClinicDto createClinic(ClinicDto clinicDto) {
        Clinic clinic = clinicMapper.toEntity(clinicDto);
        Clinic savedClinic = clinicRepository.save(clinic);
        return clinicMapper.toDto(savedClinic);
    }

    @Override
    @Transactional
    public ClinicDto updateClinic(Integer clinicId, ClinicDto clinicDto) {
        Clinic existingClinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic not found with id: " + clinicId));

        existingClinic.setName(clinicDto.getName());
        existingClinic.setAddress(clinicDto.getAddress());
        existingClinic.setTelephoneNumber(clinicDto.getTelephoneNumber());
        existingClinic.setType(clinicDto.getType());
        existingClinic.setOpeningHours(clinicDto.getOpeningHours());
        existingClinic.setClosingHours(clinicDto.getClosingHours());

        Clinic updatedClinic = clinicRepository.save(existingClinic);
        return clinicMapper.toDto(updatedClinic);
    }

    @Override
    @Transactional
    public void deleteClinic(Integer clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic not found with id: " + clinicId));
        clinicRepository.deleteById(clinicId);
    }

    @Override
    public ClinicDto getClinicById(Integer clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic not found with id: " + clinicId));
        return clinicMapper.toDto(clinic);
    }

    @Override
    public List<ClinicDto> getAllClinics() {
        List<Clinic> clinics = clinicRepository.findAll();
        return clinics.stream().map(clinicMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ClinicDto> getClinicsByType(String type) {
        List<Clinic> clinics = clinicRepository.findByType(type);
        return clinics.stream().map(clinicMapper::toDto).collect(Collectors.toList());
    }
    
    @Override
    public ClinicDto getClinicByName(String name) {
        Clinic clinic = clinicRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic not found with name: " + name));
        return clinicMapper.toDto(clinic);
    }

    @Override
    @Transactional
    public List<ClinicDto> importClinics(List<ClinicDto> clinics) {
        List<Clinic> clinicEntities = clinics.stream()
                .map(clinicMapper::toEntity)
                .collect(Collectors.toList());

        List<Clinic> savedClinics = clinicRepository.saveAll(clinicEntities);

        return savedClinics.stream()
                .map(clinicMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClinicDto setClinicHours(Integer clinicId, String openingHours, String closingHours) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundExecption("Clinic not found with id: " + clinicId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        clinic.setOpeningHours(LocalTime.parse(openingHours, formatter));
        clinic.setClosingHours(LocalTime.parse(closingHours, formatter));

        Clinic updatedClinic = clinicRepository.save(clinic);
        return clinicMapper.toDto(updatedClinic);
    }
}
