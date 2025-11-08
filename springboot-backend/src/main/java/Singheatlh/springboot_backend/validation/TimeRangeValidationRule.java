package Singheatlh.springboot_backend.validation;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.util.TimeRangeValidator;
import org.springframework.stereotype.Component;

/**
 * Validates that end datetime is after start datetime for schedules.
 * Delegates to TimeRangeValidator utility for the actual validation logic.
 */
@Component
public class TimeRangeValidationRule implements ScheduleValidationRule {

    @Override
    public void validate(ScheduleDto scheduleDto) {
        TimeRangeValidator.validateTimeRange(
            scheduleDto.getStartDatetime(),
            scheduleDto.getEndDatetime(),
            "Start and end datetime must not be null"
        );
    }
}
