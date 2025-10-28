package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.entity.Schedule;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.ScheduleMapper;
import Singheatlh.springboot_backend.repository.ScheduleRepository;
import Singheatlh.springboot_backend.service.*;
import Singheatlh.springboot_backend.util.EntityDtoConverter;
import Singheatlh.springboot_backend.util.TimeProvider;
import Singheatlh.springboot_backend.validation.ScheduleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of all Schedule service interfaces
 * Refactored to follow SOLID principles:
 * - Single Responsibility: Each method has one clear purpose
 * - Open/Closed: Validation is extensible via ScheduleValidator
 * - Liskov Substitution: Properly implements all interface contracts
 * - Interface Segregation: Implements 4 focused interfaces instead of 1 fat interface
 * - Dependency Inversion: Depends on abstractions (TimeProvider, ScheduleValidator)
 */
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements
        ScheduleCommandService,
        ScheduleQueryService,
        ScheduleAvailabilityService,
        ScheduleSearchService,
        ScheduleService { // Keep for backward compatibility

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    private final ScheduleValidator scheduleValidator;
    private final EntityDtoConverter converter;
    private final TimeProvider timeProvider;

    // ========== Helper Methods ==========

    /**
     * Find schedule by ID or throw exception
     * Eliminates duplicate exception handling code
     */
    private Schedule findScheduleOrThrow(String id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExecption(
                        "Schedule does not exist with id: " + id));
    }

    /**
     * Convert entity list to DTO list
     * Eliminates duplicate stream/map/collect code
     */
    private List<ScheduleDto> toDtoList(List<Schedule> schedules) {
        return converter.convertList(schedules, scheduleMapper::toDto);
    }

    // ========== Command Service Methods (Write Operations) ==========

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        // Validation delegated to extensible validator chain
        scheduleValidator.validate(scheduleDto);

        // Convert and save
        Schedule schedule = scheduleMapper.toEntity(scheduleDto);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return scheduleMapper.toDto(savedSchedule);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ScheduleDto updateSchedule(ScheduleDto scheduleDto) {
        // Find existing schedule
        Schedule schedule = findScheduleOrThrow(scheduleDto.getScheduleId());

        // Validation delegated to extensible validator chain
        scheduleValidator.validate(scheduleDto);

        // Update fields
        schedule.setStartDatetime(scheduleDto.getStartDatetime());
        schedule.setEndDatetime(scheduleDto.getEndDatetime());
        schedule.setType(scheduleDto.getType());

        // Save and return
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return scheduleMapper.toDto(savedSchedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(String id) {
        findScheduleOrThrow(id);
        scheduleRepository.deleteById(id);
    }

    // ========== Query Service Methods (Read Operations) ==========

    @Override
    public ScheduleDto getById(String id) {
        Schedule schedule = findScheduleOrThrow(id);
        return scheduleMapper.toDto(schedule);
    }

    @Override
    public List<ScheduleDto> getAllSchedules() {
        return toDtoList(scheduleRepository.findAll());
    }

    @Override
    public List<ScheduleDto> getSchedulesByDoctorId(String doctorId) {
        return toDtoList(scheduleRepository.findByDoctorId(doctorId));
    }

    @Override
    public List<ScheduleDto> getSchedulesByType(ScheduleType type) {
        return toDtoList(scheduleRepository.findByType(type));
    }

    // ========== Availability Service Methods ==========

    @Override
    public List<ScheduleDto> getAvailableSchedulesByDoctor(String doctorId) {
        return toDtoList(
                scheduleRepository.findByDoctorIdAndType(doctorId, ScheduleType.AVAILABLE)
        );
    }

    @Override
    public List<ScheduleDto> getAvailableSchedulesByDoctorAndDateRange(
            String doctorId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return toDtoList(
                scheduleRepository.findAvailableSchedulesByDoctorAndDateRange(
                        doctorId, startDate, endDate
                )
        );
    }

    @Override
    public List<ScheduleDto> getUpcomingAvailableSchedules(String doctorId) {
        // Uses TimeProvider abstraction for testability
        return toDtoList(
                scheduleRepository.findUpcomingAvailableSchedules(
                        doctorId, timeProvider.now()
                )
        );
    }

    @Override
    public boolean hasOverlappingSchedule(String doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        return scheduleRepository.existsOverlappingSchedule(doctorId, startTime, endTime);
    }

    // ========== Search Service Methods ==========

    @Override
    public List<ScheduleDto> getSchedulesInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return toDtoList(
                scheduleRepository.findSchedulesInDateRange(startDate, endDate)
        );
    }
}
