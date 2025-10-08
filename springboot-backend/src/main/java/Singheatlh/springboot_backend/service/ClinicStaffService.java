package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ClinicStaffDto;

import java.util.List;


public interface ClinicStaffService {
    ClinicStaffDto getById(Long id);
    ClinicStaffDto create(ClinicStaffDto clinicDto, String hashedPassword);
    ClinicStaffDto update(ClinicStaffDto clinicDto);
    List<ClinicStaffDto> getAllClinicStaff();
    List<ClinicStaffDto> getClinicStaffByName(String name);
    void  deleteClinicStaffBy(Long id);
}
