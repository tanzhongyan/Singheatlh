package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ClinicStatisticsDto;
import java.time.LocalDate;

public interface ClinicMonitoringService {
    /**
     * Get comprehensive statistics for a clinic on a specific date
     * @param clinicId The clinic ID
     * @param date The date to get statistics for (defaults to today if null)
     * @return Clinic statistics DTO
     */
    ClinicStatisticsDto getClinicStatistics(Integer clinicId, LocalDate date);
}

