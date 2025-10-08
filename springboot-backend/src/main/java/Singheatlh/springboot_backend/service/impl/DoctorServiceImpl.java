package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.DoctorDto;
import Singheatlh.springboot_backend.entity.Doctor;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.DoctorMapper;
import Singheatlh.springboot_backend.repository.DoctorRepository;
import Singheatlh.springboot_backend.service.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private DoctorRepository doctorRepository;

    @Override
    public DoctorDto getById(Long id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundExecption("Doctor does not exist with the given id " + id)
        );
        return DoctorMapper.mapToDoctorDto(doctor);
    }

    @Override
    public DoctorDto createDoctor(DoctorDto doctorDto) {
        Doctor doctor = DoctorMapper.mapToDoctor(doctorDto);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return DoctorMapper.mapToDoctorDto(savedDoctor);
    }

    @Override
    public List<DoctorDto> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        return doctors.stream()
                .map(DoctorMapper::mapToDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorDto> getDoctorsByClinicId(Integer clinicId) {
        List<Doctor> doctors = doctorRepository.findByClinicId(clinicId);
        return doctors.stream()
                .map(DoctorMapper::mapToDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorDto updateDoctor(DoctorDto doctorDto) {
        Doctor doctor = doctorRepository.findById(doctorDto.getDoctorId()).orElseThrow(
                () -> new ResourceNotFoundExecption("Doctor does not exist with the given id " + doctorDto.getDoctorId())
        );
        doctor.setName(doctorDto.getName());
        doctor.setSchedule(doctorDto.getSchedule());
        doctor.setClinicId(doctorDto.getClinicId());
        Doctor savedDoctor = doctorRepository.save(doctor);
        return DoctorMapper.mapToDoctorDto(savedDoctor);
    }

    @Override
    public void deleteDoctor(Long id) {
        doctorRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundExecption("Doctor does not exist with the given id " + id)
        );
        doctorRepository.deleteById(id);
    }
}
