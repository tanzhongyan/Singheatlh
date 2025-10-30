package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.dto.SlotDto;
import Singheatlh.springboot_backend.entity.Doctor;
import Singheatlh.springboot_backend.entity.Schedule;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.ScheduleMapper;
import Singheatlh.springboot_backend.repository.DoctorRepository;
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
import java.util.*;

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
    private final DoctorRepository doctorRepository;

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

    // ========= Slot Service Methods ========
    @Override
    public Map<Date, List<SlotDto>> generateDoctorSlots(String id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExecption(
                        "Doctor does not exist with id: " + id));;

        List<ScheduleDto> scheduleDtos = getAvailableSchedulesByDoctor(id);
        Map<Date, List<SlotDto>> slotsByDate = new HashMap<>();

        int slotDurationMinutes = doctor.getAppointmentDurationInMinutes();

        for (ScheduleDto schedule : scheduleDtos) {
            LocalDateTime currentSlotStart = schedule.getStartDatetime();
            LocalDateTime scheduleEnd = schedule.getEndDatetime();

            // Generate slots until we can't fit another full slot
            while (currentSlotStart.plusMinutes(slotDurationMinutes).isBefore(scheduleEnd) ||
                    currentSlotStart.plusMinutes(slotDurationMinutes).isEqual(scheduleEnd)) {

                LocalDateTime currentSlotEnd = currentSlotStart.plusMinutes(slotDurationMinutes);

                // Create slot
                SlotDto slot = new SlotDto();
                slot.setStartDatetime(currentSlotStart);
                slot.setEndDatetime(currentSlotEnd);

                // Convert to Date for grouping (using SQL Date or java.util.Date)
                Date date = java.sql.Date.valueOf(currentSlotStart.toLocalDate());

                // Add to map
                slotsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(slot);

                // Move to next slot
                currentSlotStart = currentSlotEnd;
            }
        }

        return slotsByDate;
    }

}
