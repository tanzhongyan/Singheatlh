package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.dto.*;
import Singheatlh.springboot_backend.dto.request.*;
import Singheatlh.springboot_backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/system-administrators")
@RequiredArgsConstructor
public class SystemAdministratorController {
    private final SystemAdministratorService systemAdministratorService;
    private final UserService userService;
    private final DoctorService doctorService;
    private final ClinicManagementService clinicManagementService;
    private final SystemMonitoringService systemMonitoringService;
    private final SystemBackupService systemBackupService;

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

    @GetMapping("/users/paginated")
    public ResponseEntity<Object> getUsersPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(userService.getUsersWithPagination(page, pageSize, search, role));
    }

    @GetMapping("/users/count")
    public ResponseEntity<Integer> getUserCount() {
        int count = userService.getUserCount();
        return ResponseEntity.ok(count);
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

    @GetMapping("/doctors/paginated")
    public ResponseEntity<Object> getDoctorsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(doctorService.getDoctorsWithPagination(page, pageSize, search));
    }

    @GetMapping("/doctors/count")
    public ResponseEntity<Integer> getDoctorCount() {
        int count = doctorService.getDoctorCount();
        return ResponseEntity.ok(count);
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
    public ResponseEntity<String> deleteDoctor(@PathVariable String doctorId) {
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.ok("Doctor deleted successfully!");
    }

    // Clinic Management
    @GetMapping("/clinics/paginated")
    public ResponseEntity<Object> getClinicsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(clinicManagementService.getClinicsWithPagination(page, pageSize, search));
    }

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

    @PostMapping("/clinics")
    public ResponseEntity<ClinicDto> createClinic(@RequestBody ClinicDto clinicDto) {
        ClinicDto createdClinic = clinicManagementService.createClinic(clinicDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClinic);
    }

    @PutMapping("/clinics/{clinicId}")
    public ResponseEntity<ClinicDto> updateClinic(@PathVariable Integer clinicId, @RequestBody ClinicDto clinicDto) {
        ClinicDto updatedClinic = clinicManagementService.updateClinic(clinicId, clinicDto);
        return ResponseEntity.ok(updatedClinic);
    }

    @DeleteMapping("/clinics/{clinicId}")
    public ResponseEntity<String> deleteClinic(@PathVariable Integer clinicId) {
        clinicManagementService.deleteClinic(clinicId);
        return ResponseEntity.ok("Clinic deleted successfully!");
    }

    @GetMapping("/clinics/count")
    public ResponseEntity<Integer> getClinicCount() {
        int count = clinicManagementService.getClinicCount();
        return ResponseEntity.ok(count);
    }

    // System Monitoring
    @GetMapping("/monitoring/statistics")
    public ResponseEntity<SystemStatisticsDto> getSystemStatistics() {
        SystemStatisticsDto statistics = systemMonitoringService.getSystemStatistics();
        return ResponseEntity.ok(statistics);
    }

    // System Backup Management
    @PostMapping("/backup/create")
    public ResponseEntity<BackupStatusDto> createBackup() {
        BackupStatusDto backup = systemBackupService.createBackup();
        return ResponseEntity.status(HttpStatus.CREATED).body(backup);
    }

    @GetMapping("/backup/history")
    public ResponseEntity<List<BackupStatusDto>> getBackupHistory() {
        List<BackupStatusDto> backups = systemBackupService.getBackupHistory();
        return ResponseEntity.ok(backups);
    }

    @GetMapping("/backup/download/{backupId}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String backupId) {
        Resource resource = systemBackupService.downloadBackup(backupId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backupId + ".zip\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                .body(resource);
    }

    @PostMapping("/backup/restore/{backupId}")
    public ResponseEntity<String> restoreBackup(@PathVariable String backupId) {
        systemBackupService.restoreBackup(backupId);
        return ResponseEntity.ok("Backup restored successfully!");
    }

    @DeleteMapping("/backup/{backupId}")
    public ResponseEntity<String> deleteBackup(@PathVariable String backupId) {
        systemBackupService.deleteBackup(backupId);
        return ResponseEntity.ok("Backup deleted successfully!");
    }

    // Exception Handlers

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> errorResponse = new HashMap<>();
        String message = e.getMessage();

        // Handle duplicate email errors
        if (message != null && message.contains("already exists")) {
            errorResponse.put("error", "Duplicate Email");
            errorResponse.put("message", "This email address is already registered in the system. Please use a different email.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        // Handle other runtime errors
        errorResponse.put("error", "Request Failed");
        errorResponse.put("message", message != null ? message : "An error occurred while processing your request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
