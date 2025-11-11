package Singheatlh.springboot_backend.strategy.cancellation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Context object containing all information needed for appointment cancellation.
 * Encapsulates cancellation parameters to avoid long parameter lists.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationContext {

    /**
     * ID of the user canceling the appointment (patient or staff).
     */
    private UUID cancelledBy;

    /**
     * Reason for cancellation.
     * Optional for patients, mandatory for staff.
     */
    private String reason;

    /**
     * Flag indicating if the cancellation is being performed by staff.
     * Used to select the appropriate cancellation strategy.
     */
    private boolean isStaff;

    /**
     * Current timestamp for validation purposes.
     * Allows for consistent time checking and testability.
     */
    private LocalDateTime now;
}
