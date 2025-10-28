package Singheatlh.springboot_backend.validation;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Orchestrates all schedule validation rules
 * Following Chain of Responsibility pattern
 * New rules can be added by creating new ScheduleValidationRule implementations
 */
@Component
@RequiredArgsConstructor
public class ScheduleValidator {

    private final List<ScheduleValidationRule> validationRules;

    /**
     * Validate a schedule against all registered rules
     * @param scheduleDto The schedule to validate
     * @throws IllegalArgumentException if any validation rule fails
     */
    public void validate(ScheduleDto scheduleDto) {
        validationRules.forEach(rule -> rule.validate(scheduleDto));
    }
}
