package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.DoctorDto;
import Singheatlh.springboot_backend.entity.Clinic;
import Singheatlh.springboot_backend.entity.Doctor;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.DoctorMapper;
import Singheatlh.springboot_backend.repository.ClinicRepository;
import Singheatlh.springboot_backend.repository.DoctorRepository;
import Singheatlh.springboot_backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorMapper doctorMapper;

    @Override
    public DoctorDto getById(String id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundExecption("Doctor does not exist with the given id " + id));
        return doctorMapper.toDto(doctor);
    }

    @Override
    @Transactional
    public DoctorDto createDoctor(DoctorDto doctorDto) {
        Doctor doctor = doctorMapper.toEntity(doctorDto);

        if (doctorDto.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(doctorDto.getClinicId()).orElseThrow(
                    () -> new ResourceNotFoundExecption(
                            "Clinic does not exist with the given id " + doctorDto.getClinicId()));
            doctor.setClinic(clinic);
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDto(savedDoctor);
    }

    @Override
    public List<DoctorDto> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        return doctors.stream()
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorDto> getDoctorsByClinicId(Integer clinicId) {
        List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);
        return doctors.stream()
                .map(doctorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoctorDto updateDoctor(DoctorDto doctorDto) {
        Doctor doctor = doctorRepository.findById(doctorDto.getDoctorId()).orElseThrow(
                () -> new ResourceNotFoundExecption(
                        "Doctor does not exist with the given id " + doctorDto.getDoctorId()));

        doctor.setName(doctorDto.getName());

        if (doctorDto.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(doctorDto.getClinicId()).orElseThrow(
                    () -> new ResourceNotFoundExecption(
                            "Clinic does not exist with the given id " + doctorDto.getClinicId()));
            doctor.setClinicId(doctorDto.getClinicId());
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDto(savedDoctor);
    }

    @Override
    @Transactional
    public void deleteDoctor(String id) {
        doctorRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundExecption("Doctor does not exist with the given id " + id));
        doctorRepository.deleteById(id);
    }

    @Override
    public int getDoctorCount() {
        return (int) doctorRepository.count();
    }
}
