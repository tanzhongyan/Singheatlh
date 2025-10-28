package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {

    // CRUD operations
    ScheduleDto getById(String id);
    ScheduleDto createSchedule(ScheduleDto scheduleDto);
    List<ScheduleDto> getAllSchedules();
    ScheduleDto updateSchedule(ScheduleDto scheduleDto);
    void deleteSchedule(String id);

    // Custom queries
    List<ScheduleDto> getSchedulesByDoctorId(String doctorId);
    List<ScheduleDto> getSchedulesByType(ScheduleType type);
    List<ScheduleDto> getAvailableSchedulesByDoctor(String doctorId);
    List<ScheduleDto> getSchedulesInDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<ScheduleDto> getAvailableSchedulesByDoctorAndDateRange(
            String doctorId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    List<ScheduleDto> getUpcomingAvailableSchedules(String doctorId);

    // Validation
    boolean hasOverlappingSchedule(String doctorId, LocalDateTime startTime, LocalDateTime endTime);
}
