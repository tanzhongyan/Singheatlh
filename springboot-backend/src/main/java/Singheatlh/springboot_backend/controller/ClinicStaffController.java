package Singheatlh.springboot_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import Singheatlh.springboot_backend.dto.ClinicStatisticsDto;
import Singheatlh.springboot_backend.service.ClinicMonitoringService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/clinic-staff")
@RequiredArgsConstructor
public class ClinicStaffController {
    
    private final ClinicMonitoringService clinicMonitoringService;
    
    /**
     * Get monitoring statistics for a specific clinic
     * @param clinicId The clinic ID
     * @param date Optional date parameter (defaults to today)
     * @return Clinic statistics
     */
    @GetMapping("/monitoring/statistics/{clinicId}")
    public ResponseEntity<?> getClinicStatistics(
            @PathVariable Integer clinicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            ClinicStatisticsDto statistics = clinicMonitoringService.getClinicStatistics(clinicId, date);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Failed to fetch clinic statistics");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("clinicId", clinicId.toString());
            errorResponse.put("date", date != null ? date.toString() : "today");
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
