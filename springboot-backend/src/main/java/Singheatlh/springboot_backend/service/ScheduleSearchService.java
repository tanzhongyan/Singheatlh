package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ScheduleDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Search Service Interface for Schedule complex queries
 * Following Interface Segregation Principle
 */
public interface ScheduleSearchService {

    /**
     * Get schedules within a date range
     * @param startDate The start date
     * @param endDate The end date
     * @return List of schedules in the date range
     */
    List<ScheduleDto> getSchedulesInDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
