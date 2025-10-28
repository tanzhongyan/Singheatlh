package Singheatlh.springboot_backend.validation;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import org.springframework.stereotype.Component;

/**
 * Validates that end datetime is after start datetime
 */
@Component
public class TimeRangeValidationRule implements ScheduleValidationRule {

    @Override
    public void validate(ScheduleDto scheduleDto) {
        if (scheduleDto.getEndDatetime() == null || scheduleDto.getStartDatetime() == null) {
            throw new IllegalArgumentException("Start and end datetime must not be null");
        }

        if (scheduleDto.getEndDatetime().isBefore(scheduleDto.getStartDatetime()) ||
            scheduleDto.getEndDatetime().isEqual(scheduleDto.getStartDatetime())) {
            throw new IllegalArgumentException("End datetime must be after start datetime");
        }
    }
}
