package Singheatlh.springboot_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for rescheduling appointments.
 * Supports both time-only rescheduling (keep same doctor/clinic)
 * and full rescheduling (change doctor and/or clinic).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleAppointmentRequest {

    /**
     * New appointment date and time (required)
     */
    private LocalDateTime newDateTime;

    /**
     * New doctor ID (optional - if null, keeps current doctor)
     * Format: String ID like 'D000000012'
     */
    private String newDoctorId;

    /**
     * New clinic ID (optional - if null, keeps current clinic)
     */
    private Integer newClinicId;
}
