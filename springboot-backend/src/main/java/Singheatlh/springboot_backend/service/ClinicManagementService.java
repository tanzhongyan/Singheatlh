package Singheatlh.springboot_backend.service;

import java.util.List;

import Singheatlh.springboot_backend.dto.ClinicDto;
import Singheatlh.springboot_backend.dto.PaginatedResponse;

public interface ClinicManagementService {

    // Create a new clinic
    ClinicDto createClinic(ClinicDto clinicDto);

    // Update existing clinic
    ClinicDto updateClinic(Integer clinicId, ClinicDto clinicDto);

    // Delete clinic by ID
    void deleteClinic(Integer clinicId);

    // Get clinic by ID
    ClinicDto getClinicById(Integer clinicId);

    // Get all clinics
    List<ClinicDto> getAllClinics();

    // Get clinics by type (GP or Specialist)
    List<ClinicDto> getClinicsByType(String type);

    // Get clinic by name
    ClinicDto getClinicByName(String name);

    // Import clinic data (for bulk operations)
    List<ClinicDto> importClinics(List<ClinicDto> clinics);

    // Set clinic hours
    ClinicDto setClinicHours(Integer clinicId, String openingHours, String closingHours);

    // Get Clinic Count
    int getClinicCount();

    // Get clinics with pagination and search
    PaginatedResponse<ClinicDto> getClinicsWithPagination(int page, int pageSize, String search);
}
