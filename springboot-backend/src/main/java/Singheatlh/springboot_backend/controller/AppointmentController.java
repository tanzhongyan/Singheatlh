package Singheatlh.springboot_backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Singheatlh.springboot_backend.dto.AppointmentDto;
import Singheatlh.springboot_backend.dto.CreateAppointmentRequest;
import Singheatlh.springboot_backend.dto.ErrorResponse;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
    
    @PostMapping
    public ResponseEntity<AppointmentDto> createAppointment(@RequestBody CreateAppointmentRequest request) {
        try {
            AppointmentDto createdAppointment = appointmentService.createAppointment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    // Get all appointments
    @GetMapping
    public ResponseEntity<List<AppointmentDto>> getAllAppointments() {
        try {
            List<AppointmentDto> appointments = appointmentService.getAllAppointments();
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get appointments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByStatus(status);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Convenience endpoints for specific statuses
    @GetMapping("/upcoming")
    public ResponseEntity<List<AppointmentDto>> getUpcomingAppointments() {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByStatus(AppointmentStatus.Upcoming);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/completed")
    public ResponseEntity<List<AppointmentDto>> getCompletedAppointments() {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByStatus(AppointmentStatus.Completed);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/cancelled")
    public ResponseEntity<List<AppointmentDto>> getCancelledAppointments() {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByStatus(AppointmentStatus.Cancelled);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/missed")
    public ResponseEntity<List<AppointmentDto>> getMissedAppointments() {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByStatus(AppointmentStatus.Missed);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/ongoing")
    public ResponseEntity<List<AppointmentDto>> getOngoingAppointments() {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByStatus(AppointmentStatus.Ongoing);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get appointments by patient ID
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByPatientId(@PathVariable UUID patientId) {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByPatientId(patientId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get upcoming appointments by patient ID
    @GetMapping("/patient/{patientId}/upcoming")
    public ResponseEntity<List<AppointmentDto>> getUpcomingAppointmentsByPatientId(@PathVariable UUID patientId) {
        try {
            List<AppointmentDto> appointments = appointmentService.getUpcomingAppointmentsByPatientId(patientId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get appointments by doctor ID
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByDoctorId(@PathVariable String doctorId) {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get available time slots for a doctor on a specific date
    @GetMapping("/doctor/{doctorId}/available-slots")
    public ResponseEntity<List<LocalDateTime>> getAvailableSlots(
            @PathVariable String doctorId,
            @RequestParam String date) {
        try {
            LocalDateTime targetDate = LocalDateTime.parse(date + "T00:00:00");
            List<LocalDateTime> availableSlots = appointmentService.getAvailableSlots(doctorId, targetDate);
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get upcoming appointments by doctor ID
    @GetMapping("/doctor/{doctorId}/upcoming")
    public ResponseEntity<List<AppointmentDto>> getUpcomingAppointmentsByDoctorId(@PathVariable String doctorId) {
        try {
            List<AppointmentDto> appointments = appointmentService.getUpcomingAppointmentsByDoctorId(doctorId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get appointment by ID - MUST be last among GET mappings to avoid path conflicts
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable String id) {
        try {
            AppointmentDto appointment = appointmentService.getAppointmentById(id);
            return ResponseEntity.ok(appointment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentDto> updateAppointmentStatus(
            @PathVariable String id, 
            @RequestParam AppointmentStatus status) {
        try {
            if (status == null) {
                return ResponseEntity.badRequest().build();
            }
            AppointmentDto updatedAppointment = appointmentService.updateAppointmentStatus(id, status);
            return ResponseEntity.ok(updatedAppointment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable String id) {
        try {
            appointmentService.cancelAppointment(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Return 400 with the service message in a structured error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Convenient status update endpoints
    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentDto> completeAppointment(@PathVariable String id) {
        try {
            AppointmentDto updatedAppointment = appointmentService.updateAppointmentStatus(id, AppointmentStatus.Completed);
            return ResponseEntity.ok(updatedAppointment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/mark-missed")
    public ResponseEntity<AppointmentDto> markAppointmentAsMissed(@PathVariable String id) {
        try {
            AppointmentDto updatedAppointment = appointmentService.updateAppointmentStatus(id, AppointmentStatus.Missed);
            return ResponseEntity.ok(updatedAppointment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/start")
    public ResponseEntity<AppointmentDto> startAppointment(@PathVariable String id) {
        try {
            AppointmentDto updatedAppointment = appointmentService.updateAppointmentStatus(id, AppointmentStatus.Ongoing);
            return ResponseEntity.ok(updatedAppointment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable String id, 
            @RequestParam LocalDateTime newDateTime) {
        try {
            if (newDateTime == null) {
                return ResponseEntity.badRequest().build();
            }
            AppointmentDto rescheduledAppointment = appointmentService.rescheduleAppointment(id, newDateTime);
            return ResponseEntity.ok(rescheduledAppointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== Clinic Staff Endpoints ==========

    /**
     * Create a walk-in appointment (staff only)
     * Bypasses future time validation to allow immediate appointment creation
     * Staff can create appointments for patients who walk in without prior booking
     *
     * Exception handling delegated to GlobalExceptionHandler
     */
    @PostMapping("/walk-in")
    public ResponseEntity<AppointmentDto> createWalkInAppointment(@RequestBody CreateAppointmentRequest request) {
        AppointmentDto createdAppointment = appointmentService.createWalkInAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
    }

    /**
     * Get all appointments for a specific clinic
     * Clinic staff can view all appointments for their clinic
     */
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByClinic(@PathVariable Integer clinicId) {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByClinicId(clinicId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get appointments for a clinic filtered by status
     */
    @GetMapping("/clinic/{clinicId}/status/{status}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByClinicAndStatus(
            @PathVariable Integer clinicId,
            @PathVariable AppointmentStatus status) {
        try {
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByClinicIdAndStatus(clinicId, status);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get today's appointments for a specific clinic
     * Useful for clinic staff to see the daily schedule
     */
    @GetMapping("/clinic/{clinicId}/today")
    public ResponseEntity<List<AppointmentDto>> getTodayAppointmentsByClinic(@PathVariable Integer clinicId) {
        try {
            List<AppointmentDto> appointments = appointmentService.getTodayAppointmentsByClinicId(clinicId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get appointments for a clinic within a date range
     * Useful for viewing appointments in a specific week/month
     */
    @GetMapping("/clinic/{clinicId}/date-range")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByClinicAndDateRange(
            @PathVariable Integer clinicId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        try {
            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest().build();
            }
            List<AppointmentDto> appointments = appointmentService.getAppointmentsByClinicIdAndDateRange(
                clinicId, startDate, endDate
            );
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get upcoming appointments for a specific clinic
     * Shows all future appointments for the clinic
     */
    @GetMapping("/clinic/{clinicId}/upcoming")
    public ResponseEntity<List<AppointmentDto>> getUpcomingAppointmentsByClinic(@PathVariable Integer clinicId) {
        try {
            List<AppointmentDto> appointments = appointmentService.getUpcomingAppointmentsByClinicId(clinicId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Note: ErrorResponse class moved to shared DTO package
    // Exception handling delegated to GlobalExceptionHandler
}

