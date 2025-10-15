package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.dto.request.CreateClinicStaffRequest;
import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.service.ClinicStaffService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/clinic-staff")
public class ClinicStaffController {

    private final ClinicStaffService clinicStaffService;


    @PostMapping
    public ResponseEntity<ClinicStaffDto> createClinicStaff(@RequestBody CreateClinicStaffRequest createClinicStaffRequest) {
        ClinicStaffDto clinicStaffDto = ClinicStaffDto.builder()
                .id(createClinicStaffRequest.getId())
                .username(createClinicStaffRequest.getUsername())
                .name(createClinicStaffRequest.getName())
                .email(createClinicStaffRequest.getEmail())
                .clinicId(createClinicStaffRequest.getClinicId())
                .build();
        ClinicStaffDto createdStaff = clinicStaffService.create(clinicStaffDto);
        return ResponseEntity.ok(createdStaff);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicStaffDto> getClinicStaffById(@PathVariable String id) {
        ClinicStaffDto staff = clinicStaffService.getById(id);
        return ResponseEntity.ok(staff);
    }

    @GetMapping
    public ResponseEntity<List<ClinicStaffDto>> getAllClinicStaff() {
        List<ClinicStaffDto> staffList = clinicStaffService.getAllClinicStaff();
        return ResponseEntity.ok(staffList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicStaffDto> updateClinicStaff(@PathVariable String id,
                                                            @RequestBody ClinicStaffDto clinicStaffDto) {
        clinicStaffDto.setId(id); // ensure path ID is used
        ClinicStaffDto updatedStaff = clinicStaffService.update(clinicStaffDto);
        return ResponseEntity.ok(updatedStaff);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClinicStaff(@PathVariable String id) {
        clinicStaffService.deleteClinicStaffBy(id);
        return ResponseEntity.ok("Clinic staff deleted successfully!");
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClinicStaffDto>> searchClinicStaffByName(@RequestParam String name) {
        List<ClinicStaffDto> staffList = clinicStaffService.getClinicStaffByName(name);
        return ResponseEntity.ok(staffList);
    }

    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<ClinicStaffDto>> getStaffByClinic(@PathVariable int clinicId) {
        List<ClinicStaffDto> staffList = clinicStaffService.getClinicStaffByClinic(clinicId);
        return ResponseEntity.ok(staffList);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClinicStaffDto> getStaffByEmail(@PathVariable String email) {
        ClinicStaffDto staff = clinicStaffService.getByEmail(email);
        return ResponseEntity.ok(staff);
    }
}

