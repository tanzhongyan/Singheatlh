package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ScheduleDto;

/**
 * Command Service Interface for Schedule write operations
 * Following CQRS pattern and Interface Segregation Principle
 */
public interface ScheduleCommandService {

    /**
     * Create a new schedule
     * @param scheduleDto The schedule data to create
     * @return The created schedule
     */
    ScheduleDto createSchedule(ScheduleDto scheduleDto);

    /**
     * Update an existing schedule
     * @param scheduleDto The schedule data to update
     * @return The updated schedule
     */
    ScheduleDto updateSchedule(ScheduleDto scheduleDto);

    /**
     * Delete a schedule by ID
     * @param id The schedule ID
     */
    void deleteSchedule(String id);
}
