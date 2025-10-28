package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ScheduleDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Availability Service Interface for Schedule availability checking
 * Following Interface Segregation Principle
 * Used by appointment booking and scheduling components
 */
public interface ScheduleAvailabilityService {

    /**
     * Get available schedules for a doctor
     * @param doctorId The doctor ID
     * @return List of available schedules
     */
    List<ScheduleDto> getAvailableSchedulesByDoctor(String doctorId);

    /**
     * Get available schedules for a doctor within a date range
     * @param doctorId The doctor ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of available schedules in the date range
     */
    List<ScheduleDto> getAvailableSchedulesByDoctorAndDateRange(
            String doctorId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Get upcoming available schedules for a doctor
     * @param doctorId The doctor ID
     * @return List of upcoming available schedules
     */
    List<ScheduleDto> getUpcomingAvailableSchedules(String doctorId);

    /**
     * Check if doctor has overlapping schedule at given time
     * @param doctorId The doctor ID
     * @param startTime The start time
     * @param endTime The end time
     * @return true if there is overlap, false otherwise
     */
    boolean hasOverlappingSchedule(String doctorId, LocalDateTime startTime, LocalDateTime endTime);
}
