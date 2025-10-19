package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.dto.ClinicDto;
import Singheatlh.springboot_backend.service.ClinicManagementService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private ClinicManagementService clinicManagementService;

    // Create a new clinic
    @PostMapping("/clinics")
    public ResponseEntity<ClinicDto> createClinic(@RequestBody ClinicDto clinicDto) {
        ClinicDto createdClinic = clinicManagementService.createClinic(clinicDto);
        return new ResponseEntity<>(createdClinic, HttpStatus.CREATED);
    }

    // Update an existing clinic
    @PutMapping("/clinics/{clinicId}")
    public ResponseEntity<ClinicDto> updateClinic(
            @PathVariable Integer clinicId,
            @RequestBody ClinicDto clinicDto) {
        ClinicDto updatedClinic = clinicManagementService.updateClinic(clinicId, clinicDto);
        return ResponseEntity.ok(updatedClinic);
    }

    // Delete a clinic
    @DeleteMapping("/clinics/{clinicId}")
    public ResponseEntity<Void> deleteClinic(@PathVariable Integer clinicId) {
        clinicManagementService.deleteClinic(clinicId);
        return ResponseEntity.noContent().build();
    }

    // Get clinic by ID
    @GetMapping("/clinics/{clinicId}")
    public ResponseEntity<ClinicDto> getClinicById(@PathVariable Integer clinicId) {
        ClinicDto clinic = clinicManagementService.getClinicById(clinicId);
        return ResponseEntity.ok(clinic);
    }

    // Get all clinics
    @GetMapping("/clinics")
    public ResponseEntity<List<ClinicDto>> getAllClinics() {
        List<ClinicDto> clinics = clinicManagementService.getAllClinics();
        return ResponseEntity.ok(clinics);
    }

    // Get clinics by type
    @GetMapping("/clinics/type/{type}")
    public ResponseEntity<List<ClinicDto>> getClinicsByType(@PathVariable String type) {
        List<ClinicDto> clinics = clinicManagementService.getClinicsByType(type);
        return ResponseEntity.ok(clinics);
    }

    // Import multiple clinics
    @PostMapping("/clinics/import")
    public ResponseEntity<List<ClinicDto>> importClinics(@RequestBody List<ClinicDto> clinics) {
        List<ClinicDto> importedClinics = clinicManagementService.importClinics(clinics);
        return new ResponseEntity<>(importedClinics, HttpStatus.CREATED);
    }

    // Set clinic hours
    @PutMapping("/clinics/{clinicId}/hours")
    public ResponseEntity<ClinicDto> setClinicHours(
            @PathVariable Integer clinicId,
            @RequestParam String openingHours,
            @RequestParam String closingHours) {
        ClinicDto clinic = clinicManagementService.setClinicHours(clinicId, openingHours, closingHours);
        return ResponseEntity.ok(clinic);
    }
}
