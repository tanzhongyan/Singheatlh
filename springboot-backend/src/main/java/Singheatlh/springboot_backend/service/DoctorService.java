package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.DoctorDto;
import Singheatlh.springboot_backend.dto.PaginatedResponse;

import java.util.List;

public interface DoctorService {
    DoctorDto getById(String id);

    DoctorDto createDoctor(DoctorDto doctorDto);

    List<DoctorDto> getAllDoctors();

    List<DoctorDto> getDoctorsByClinicId(Integer clinicId);

    DoctorDto updateDoctor(DoctorDto doctorDto);

    void deleteDoctor(String id);

    int getDoctorCount();

    PaginatedResponse<DoctorDto> getDoctorsWithPagination(int page, int pageSize, String search);
}
