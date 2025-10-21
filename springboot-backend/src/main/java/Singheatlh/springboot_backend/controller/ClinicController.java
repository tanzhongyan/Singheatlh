package Singheatlh.springboot_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Singheatlh.springboot_backend.dto.ClinicDto;
import Singheatlh.springboot_backend.service.ClinicManagementService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/clinic")
public class ClinicController {

    private ClinicManagementService clinicManagementService;

    @GetMapping
    public ResponseEntity<List<ClinicDto>> getAllClinics() {
        List<ClinicDto> clinics = clinicManagementService.getAllClinics();
        return ResponseEntity.ok(clinics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicDto> getClinicById(@PathVariable("id") Integer clinicId) {
        ClinicDto clinic = clinicManagementService.getClinicById(clinicId);
        return ResponseEntity.ok(clinic);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ClinicDto>> getClinicsByType(@PathVariable("type") String type) {
        List<ClinicDto> clinics = clinicManagementService.getClinicsByType(type);
        return ResponseEntity.ok(clinics);
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<ClinicDto> getClinicByName(@PathVariable("name") String name) {
        ClinicDto clinic = clinicManagementService.getClinicByName(name);
        return ResponseEntity.ok(clinic);
    }
}
