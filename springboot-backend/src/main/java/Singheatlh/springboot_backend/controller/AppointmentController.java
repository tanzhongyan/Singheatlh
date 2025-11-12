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
import Singheatlh.springboot_backend.dto.MedicalSummaryDto;
import Singheatlh.springboot_backend.dto.request.RescheduleAppointmentRequest;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.service.AppointmentService;
import Singheatlh.springboot_backend.service.MedicalSummaryService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    private final MedicalSummaryService medicalSummaryService;
    
    @Autowired
    public AppointmentController(AppointmentService appointmentService, MedicalSummaryService medicalSummaryService) {
        this.appointmentService = appointmentService;
        this.medicalSummaryService = medicalSummaryService;
    }
    
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody CreateAppointmentRequest request) {
        try {
            AppointmentDto createdAppointment = appointmentService.createAppointment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
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

    @PutMapping("/{id}/cancel-by-staff")
    public ResponseEntity<?> cancelAppointmentByStaff(
            @PathVariable String id,
            @RequestBody Singheatlh.springboot_backend.dto.request.CancelAppointmentByStaffRequest request) {
        try {
            appointmentService.cancelAppointmentByStaff(id, request.getStaffId(), request.getReason());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
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

    @PutMapping("/{id}/reschedule-with-doctor")
    public ResponseEntity<?> rescheduleAppointmentWithDoctor(
            @PathVariable String id,
            @RequestBody RescheduleAppointmentRequest request) {
        try {
            if (request.getNewDateTime() == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("New date/time is required"));
            }
            AppointmentDto rescheduledAppointment = appointmentService.rescheduleAppointment(id, request);
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

    @PutMapping("/{id}/reschedule-by-staff")
    public ResponseEntity<?> rescheduleAppointmentByStaff(
            @PathVariable String id,
            @RequestParam LocalDateTime newDateTime) {
        try {
            if (newDateTime == null) {
                return ResponseEntity.badRequest().build();
            }
            AppointmentDto rescheduledAppointment = appointmentService.rescheduleAppointmentByStaff(id, newDateTime);
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

    @PutMapping("/{id}/reschedule-by-staff-with-doctor")
    public ResponseEntity<?> rescheduleAppointmentByStaffWithDoctor(
            @PathVariable String id,
            @RequestBody RescheduleAppointmentRequest request) {
        try {
            if (request.getNewDateTime() == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("New date/time is required"));
            }
            AppointmentDto rescheduledAppointment = appointmentService.rescheduleAppointmentByStaff(id, request);
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

    /**
     * Get medical summary for a specific appointment
     * Returns the treatment summary if available
     */
    @GetMapping("/{appointmentId}/medical-summary")
    public ResponseEntity<?> getMedicalSummary(@PathVariable String appointmentId) {
        MedicalSummaryDto summary = medicalSummaryService.getMedicalSummaryByAppointmentId(appointmentId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("No medical summary found for this appointment"));
        }
        return ResponseEntity.ok(summary);
    }

    // Note: ErrorResponse class moved to shared DTO package
    // Exception handling delegated to GlobalExceptionHandler for most endpoints
}

