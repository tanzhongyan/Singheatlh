package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;

import java.util.List;

/**
 * Query Service Interface for Schedule read operations
 * Following CQRS pattern and Interface Segregation Principle
 */
public interface ScheduleQueryService {

    /**
     * Get schedule by ID
     * @param id The schedule ID
     * @return The schedule
     */
    ScheduleDto getById(String id);

    /**
     * Get all schedules
     * @return List of all schedules
     */
    List<ScheduleDto> getAllSchedules();

    /**
     * Get schedules by doctor ID
     * @param doctorId The doctor ID
     * @return List of schedules for the doctor
     */
    List<ScheduleDto> getSchedulesByDoctorId(String doctorId);

    /**
     * Get schedules by type
     * @param type The schedule type (AVAILABLE/UNAVAILABLE)
     * @return List of schedules of the given type
     */
    List<ScheduleDto> getSchedulesByType(ScheduleType type);
}
