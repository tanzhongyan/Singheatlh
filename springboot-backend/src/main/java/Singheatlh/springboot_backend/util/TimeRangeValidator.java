package Singheatlh.springboot_backend.util;

import java.time.LocalDateTime;

/**
 * Utility class for validating time ranges.
 * Provides reusable time validation logic across different contexts (appointments, schedules, etc.).
 *
 * This eliminates code duplication and provides a single source of truth for time validation.
 */
public class TimeRangeValidator {

    private TimeRangeValidator() {
        // Private constructor to prevent instantiation of utility class
    }

    /**
     * Validates that end datetime is after start datetime.
     *
     * @param startDatetime The start datetime
     * @param endDatetime The end datetime
     * @throws IllegalArgumentException if start or end is null, or if end is not after start
     */
    public static void validateTimeRange(LocalDateTime startDatetime, LocalDateTime endDatetime) {
        validateTimeRange(startDatetime, endDatetime, "Start and end datetime must not be null");
    }

    /**
     * Validates that end datetime is after start datetime with custom null error message.
     *
     * @param startDatetime The start datetime
     * @param endDatetime The end datetime
     * @param nullErrorMessage Custom error message for null values
     * @throws IllegalArgumentException if start or end is null, or if end is not after start
     */
    public static void validateTimeRange(LocalDateTime startDatetime, LocalDateTime endDatetime, String nullErrorMessage) {
        if (startDatetime == null || endDatetime == null) {
            throw new IllegalArgumentException(nullErrorMessage);
        }

        if (endDatetime.isBefore(startDatetime) || endDatetime.isEqual(startDatetime)) {
            throw new IllegalArgumentException("End datetime must be after start datetime");
        }
    }

    /**
     * Validates that end datetime is after start datetime.
     * Assumes non-null values (for contexts where null checking is done separately).
     *
     * @param startDatetime The start datetime (must not be null)
     * @param endDatetime The end datetime (must not be null)
     * @throws IllegalArgumentException if end is not after start
     */
    public static void validateTimeRangeNonNull(LocalDateTime startDatetime, LocalDateTime endDatetime) {
        if (endDatetime.isBefore(startDatetime) || endDatetime.isEqual(startDatetime)) {
            throw new IllegalArgumentException("End datetime must be after start datetime");
        }
    }
}
