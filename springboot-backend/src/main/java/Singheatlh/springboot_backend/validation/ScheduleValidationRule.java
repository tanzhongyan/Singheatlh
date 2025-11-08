package Singheatlh.springboot_backend.validation;

import Singheatlh.springboot_backend.dto.ScheduleDto;

/**
 * Interface for schedule validation rules.
 * Following Chain of Responsibility pattern and Open/Closed Principle.
 * New validation rules can be added without modifying existing code.
 *
 * Extends the generic ValidationRule interface for type safety and consistency.
 */
public interface ScheduleValidationRule extends ValidationRule<ScheduleDto> {
    // Inherits: void validate(ScheduleDto scheduleDto);
}
