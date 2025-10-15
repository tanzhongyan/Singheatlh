package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ClinicStaffDto;

import java.util.List;


public interface ClinicStaffService {
    ClinicStaffDto getById(String id);
    ClinicStaffDto create(ClinicStaffDto clinicDto);
    ClinicStaffDto update(ClinicStaffDto clinicDto);
    List<ClinicStaffDto> getAllClinicStaff();
    List<ClinicStaffDto> getClinicStaffByName(String name);
    void  deleteClinicStaffBy(String id);
    List<ClinicStaffDto> getClinicStaffByClinic(int clinicId);
    ClinicStaffDto getByEmail(String email);
}
