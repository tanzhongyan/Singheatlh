package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.controller.request.CreateUserRequest;
import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.service.ClinicStaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinic-staff")
public class ClinicStaffController {

    private final ClinicStaffService clinicStaffService;

    public ClinicStaffController(ClinicStaffService clinicStaffService) {
        this.clinicStaffService = clinicStaffService;
    }

    @PostMapping
    public ResponseEntity<ClinicStaffDto> createClinicStaff(@RequestBody CreateUserRequest createUserRequest) {
        ClinicStaffDto clinicStaffDto = ClinicStaffDto.builder()
                .username(createUserRequest.getUsername())
                .name(createUserRequest.getName())
                .email(createUserRequest.getEmail())
                .build();
        ClinicStaffDto createdStaff = clinicStaffService.create(clinicStaffDto, createUserRequest.getHashedPassword());
        return ResponseEntity.ok(createdStaff);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicStaffDto> getClinicStaffById(@PathVariable Long id) {
        ClinicStaffDto staff = clinicStaffService.getById(id);
        return ResponseEntity.ok(staff);
    }

    @GetMapping
    public ResponseEntity<List<ClinicStaffDto>> getAllClinicStaff() {
        List<ClinicStaffDto> staffList = clinicStaffService.getAllClinicStaff();
        return ResponseEntity.ok(staffList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicStaffDto> updateClinicStaff(@PathVariable Long id,
                                                            @RequestBody ClinicStaffDto clinicStaffDto) {
        clinicStaffDto.setId(id); // ensure path ID is used
        ClinicStaffDto updatedStaff = clinicStaffService.update(clinicStaffDto);
        return ResponseEntity.ok(updatedStaff);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClinicStaff(@PathVariable Long id) {
        clinicStaffService.deleteClinicStaffBy(id);
        return ResponseEntity.ok("Clinic staff deleted successfully!");
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClinicStaffDto>> searchClinicStaffByName(@RequestParam String name) {
        List<ClinicStaffDto> staffList = clinicStaffService.getClinicStaffByName(name);
        return ResponseEntity.ok(staffList);
    }
}
