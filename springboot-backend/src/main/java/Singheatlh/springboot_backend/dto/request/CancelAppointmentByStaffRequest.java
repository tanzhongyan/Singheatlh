package Singheatlh.springboot_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for clinic staff to cancel appointments on behalf of patients.
 * Unlike patient self-cancellation, staff can cancel at any time and must provide a reason.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelAppointmentByStaffRequest {

    /**
     * The ID of the staff member performing the cancellation
     */
    private UUID staffId;

    /**
     * The reason for cancellation (mandatory for staff)
     */
    private String reason;
}
