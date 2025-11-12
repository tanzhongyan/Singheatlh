package Singheatlh.springboot_backend.service;

import java.time.LocalDateTime;

/**
 * Separates calculation logic from business logic
 */
public interface QueueNumberCalculator {
    
    /**
     * Calculate the next queue number for a doctor
     * @param doctorId The doctor ID
     * @param now Current time
     * @return Next queue number
     */
    Integer calculateNextQueueNumber(String doctorId, LocalDateTime now);
    
    /**
     * Calculate the next ticket number for the day (per clinic)
     * @param clinicId The clinic ID
     * @param now Current time
     * @return Next ticket number for the day
     */
    Integer calculateTicketNumberForDay(Integer clinicId, LocalDateTime now);
}

