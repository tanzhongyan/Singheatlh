package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.dto.PatientDto;
import Singheatlh.springboot_backend.dto.SystemAdministratorDto;
import Singheatlh.springboot_backend.dto.request.CreateClinicStaffRequest;
import Singheatlh.springboot_backend.dto.request.CreatePatientRequest;

import java.util.List;

public interface SystemAdministratorService {
    SystemAdministratorDto getById(String id);
    SystemAdministratorDto createSystemAdministrator(SystemAdministratorDto adminDto);
    List<SystemAdministratorDto> getAllSystemAdministrators();
    SystemAdministratorDto updateSystemAdministrator(SystemAdministratorDto adminDto);
    void deleteSystemAdministrator(String id);

    PatientDto createPatient(CreatePatientRequest createPatientRequest);

    ClinicStaffDto createClinicStaff(CreateClinicStaffRequest createClinicStaffRequest);
}
