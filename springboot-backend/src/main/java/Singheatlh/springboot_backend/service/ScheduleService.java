package Singheatlh.springboot_backend.service;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.dto.SlotDto;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    /*
     * Get schedules in slots
     * */
    Map<Date, List<SlotDto>> generateDoctorSlots(String id);
}
