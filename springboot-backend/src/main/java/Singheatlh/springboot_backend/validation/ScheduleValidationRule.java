package Singheatlh.springboot_backend.validation;

import Singheatlh.springboot_backend.dto.ScheduleDto;

/**
 * Interface for schedule validation rules
 * Following Chain of Responsibility pattern and Open/Closed Principle
 * New validation rules can be added without modifying existing code
 */
public interface ScheduleValidationRule {

    /**
     * Validate a schedule DTO
     * @param scheduleDto The schedule to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validate(ScheduleDto scheduleDto);
}
