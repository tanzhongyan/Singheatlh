package Singheatlh.springboot_backend.validation;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates that the schedule doesn't overlap with existing schedules
 * For create operations - checks all schedules
 * For update operations - checks all schedules except the one being updated
 */
@Component
@RequiredArgsConstructor
public class ScheduleOverlapValidationRule implements ScheduleValidationRule {

    private final ScheduleRepository scheduleRepository;

    /**
     * Custom exception for schedule overlap conflicts
     */
    public static class ScheduleOverlapException extends RuntimeException {
        public ScheduleOverlapException(String message) {
            super(message);
        }
    }

    @Override
    public void validate(ScheduleDto scheduleDto) {
        boolean hasOverlap;

        // For updates, exclude the current schedule from overlap check
        if (scheduleDto.getScheduleId() != null) {
            hasOverlap = scheduleRepository.existsOverlappingScheduleExcluding(
                    scheduleDto.getDoctorId(),
                    scheduleDto.getStartDatetime(),
                    scheduleDto.getEndDatetime(),
                    scheduleDto.getScheduleId()
            );
        } else {
            // For creates, check all schedules
            hasOverlap = scheduleRepository.existsOverlappingSchedule(
                    scheduleDto.getDoctorId(),
                    scheduleDto.getStartDatetime(),
                    scheduleDto.getEndDatetime()
            );
        }

        if (hasOverlap) {
            throw new ScheduleOverlapException(
                    "The selected time slot overlaps with an existing schedule. Please choose a different time range."
            );
        }
    }
}
