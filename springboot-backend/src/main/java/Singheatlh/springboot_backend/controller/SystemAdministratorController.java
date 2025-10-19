package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.dto.*;
import Singheatlh.springboot_backend.dto.request.*;
import Singheatlh.springboot_backend.service.ClinicManagementService;
import Singheatlh.springboot_backend.service.DoctorService;
import Singheatlh.springboot_backend.service.SystemAdministratorService;
import Singheatlh.springboot_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/system-administrators")
@RequiredArgsConstructor
public class SystemAdministratorController {
    private final SystemAdministratorService systemAdministratorService;
    private final UserService userService;
    private final DoctorService doctorService;
    private final ClinicManagementService clinicManagementService;

    @PostMapping
    public ResponseEntity<SystemAdministratorDto> createSystemAdministrator(
            @RequestBody CreateSystemAdministratorRequest createRequest) {
        SystemAdministratorDto adminDto = SystemAdministratorDto.builder()
                .userId(UUID.fromString(createRequest.getId()))
                .name(createRequest.getName())
                .email(createRequest.getEmail())
                .build();

        SystemAdministratorDto createdAdmin = systemAdministratorService.createSystemAdministrator(adminDto);
        return new ResponseEntity<>(createdAdmin, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemAdministratorDto> getSystemAdministratorById(@PathVariable String id) {
        SystemAdministratorDto admin = systemAdministratorService.getById(id);
        return ResponseEntity.ok(admin);
    }

    @GetMapping
    public ResponseEntity<List<SystemAdministratorDto>> getAllSystemAdministrators() {
        List<SystemAdministratorDto> admins = systemAdministratorService.getAllSystemAdministrators();
        return ResponseEntity.ok(admins);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SystemAdministratorDto> updateSystemAdministrator(
            @PathVariable String id,
            @RequestBody SystemAdministratorDto adminDto) {
        adminDto.setUserId(UUID.fromString(id)); // Ensure path ID is used
        SystemAdministratorDto updatedAdmin = systemAdministratorService.updateSystemAdministrator(adminDto);
        return ResponseEntity.ok(updatedAdmin);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSystemAdministrator(@PathVariable String id) {
        systemAdministratorService.deleteSystemAdministrator(id);
        return ResponseEntity.ok("System Administrator deleted successfully!");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/patient")
    public ResponseEntity<PatientDto> createPatient(@RequestBody CreatePatientRequest createPatientRequest) {
        PatientDto newPatient = systemAdministratorService.createPatient(createPatientRequest);
        return new ResponseEntity<>(newPatient, HttpStatus.CREATED);
    }

    @PostMapping("/users/staff")
    public ResponseEntity<ClinicStaffDto> createClinicStaff(@RequestBody CreateClinicStaffRequest createClinicStaffRequest) {
        ClinicStaffDto newStaff = systemAdministratorService.createClinicStaff(createClinicStaffRequest);
        return new ResponseEntity<>(newStaff, HttpStatus.CREATED);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully!");
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String userId, @RequestBody UserDto userDto) {
        userDto.setUserId(UUID.fromString(userId));
        UserDto updatedUser = userService.updateUser(userDto);
        return ResponseEntity.ok(updatedUser);
    }

    // Doctor Management
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        List<DoctorDto> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @PostMapping("/doctors")
    public ResponseEntity<DoctorDto> createDoctor(@RequestBody DoctorDto doctorDto) {
        DoctorDto newDoctor = doctorService.createDoctor(doctorDto);
        return new ResponseEntity<>(newDoctor, HttpStatus.CREATED);
    }

    @PutMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorDto> updateDoctor(@PathVariable String doctorId, @RequestBody DoctorDto doctorDto) {
        doctorDto.setDoctorId(doctorId);
        DoctorDto updatedDoctor = doctorService.updateDoctor(doctorDto);
        return ResponseEntity.ok(updatedDoctor);
    }

    @DeleteMapping("/doctors/{doctorId}")
    public ResponseEntity<String> deleteDoctor(@PathVariable Long doctorId) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.ok("Doctor deleted successfully!");
    }

    // Clinic Management
    @PutMapping("/clinics/{clinicId}/hours")
    public ResponseEntity<ClinicDto> setClinicHours(@PathVariable Integer clinicId, @RequestBody UpdateClinicHoursRequest request) {
        ClinicDto updatedClinic = clinicManagementService.setClinicHours(clinicId, request.getOpeningHours(), request.getClosingHours());
        return ResponseEntity.ok(updatedClinic);
    }

    @PostMapping("/clinics/import")
    public ResponseEntity<List<ClinicDto>> importClinics(@RequestBody List<ClinicDto> clinics) {
        List<ClinicDto> importedClinics = clinicManagementService.importClinics(clinics);
        return ResponseEntity.status(HttpStatus.CREATED).body(importedClinics);
    }

    @GetMapping("/clinics")
    public ResponseEntity<List<ClinicDto>> getAllClinics() {
        List<ClinicDto> clinics = clinicManagementService.getAllClinics();
        return ResponseEntity.ok(clinics);
    }
}
