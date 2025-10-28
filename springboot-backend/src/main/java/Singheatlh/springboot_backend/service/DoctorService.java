package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.DoctorDto;

import java.util.List;

public interface DoctorService {
    DoctorDto getById(String id);

    DoctorDto createDoctor(DoctorDto doctorDto);

    List<DoctorDto> getAllDoctors();

    List<DoctorDto> getDoctorsByClinicId(Integer clinicId);

    DoctorDto updateDoctor(DoctorDto doctorDto);

    void deleteDoctor(String id);

    int getDoctorCount();
}
