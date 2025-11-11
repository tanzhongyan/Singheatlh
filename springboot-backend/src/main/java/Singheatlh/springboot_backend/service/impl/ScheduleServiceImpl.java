package Singheatlh.springboot_backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import Singheatlh.springboot_backend.dto.PaginatedResponse;
import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.dto.SlotDto;
import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.Doctor;
import Singheatlh.springboot_backend.entity.Schedule;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.ScheduleMapper;
import Singheatlh.springboot_backend.repository.AppointmentRepository;
import Singheatlh.springboot_backend.repository.DoctorRepository;
import Singheatlh.springboot_backend.repository.ScheduleRepository;
import Singheatlh.springboot_backend.service.ScheduleAvailabilityService;
import Singheatlh.springboot_backend.service.ScheduleCommandService;
import Singheatlh.springboot_backend.service.ScheduleQueryService;
import Singheatlh.springboot_backend.service.ScheduleSearchService;
import Singheatlh.springboot_backend.service.ScheduleService;
import Singheatlh.springboot_backend.util.EntityDtoConverter;
import Singheatlh.springboot_backend.util.TimeProvider;
import Singheatlh.springboot_backend.validation.ScheduleValidator;
import lombok.RequiredArgsConstructor;

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
    private final AppointmentRepository appointmentRepository;

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

        // Generate schedule ID if not provided
        if (schedule.getScheduleId() == null || schedule.getScheduleId().isEmpty()) {
            schedule.setScheduleId(generateScheduleId());
        }

        Schedule savedSchedule = scheduleRepository.save(schedule);
        return scheduleMapper.toDto(savedSchedule);
    }

    private String generateScheduleId() {
        // Query database for maximum ID instead of fetching all schedules
        // This is much more efficient, especially as data grows
        String maxId = scheduleRepository.findMaxScheduleId()
                .orElse("S000000000");

        // Extract number and increment
        int currentNumber = Integer.parseInt(maxId.trim().substring(1));
        int nextNumber = currentNumber + 1;

        // Format with leading zeros
        return String.format("S%09d", nextNumber);
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

    @Override
    public PaginatedResponse<ScheduleDto> getSchedulesWithPaginationByDoctorId(String doctorId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Schedule> schedulesPage = scheduleRepository.findByDoctorIdPaginated(doctorId, pageable);

        List<ScheduleDto> content = toDtoList(schedulesPage.getContent());

        return PaginatedResponse.<ScheduleDto>builder()
                .content(content)
                .page(page)
                .pageSize(pageSize)
                .totalElements(schedulesPage.getTotalElements())
                .totalPages(schedulesPage.getTotalPages())
                .hasNextPage(schedulesPage.hasNext())
                .hasPreviousPage(schedulesPage.hasPrevious())
                .build();
    }

    @Override
    public PaginatedResponse<ScheduleDto> getSchedulesWithPaginationByDoctorAndDateRange(
            String doctorId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Schedule> schedulesPage = scheduleRepository.findByDoctorIdAndDateRangePaginated(doctorId, startDate, endDate, pageable);

        List<ScheduleDto> content = toDtoList(schedulesPage.getContent());

        return PaginatedResponse.<ScheduleDto>builder()
                .content(content)
                .page(page)
                .pageSize(pageSize)
                .totalElements(schedulesPage.getTotalElements())
                .totalPages(schedulesPage.getTotalPages())
                .hasNextPage(schedulesPage.hasNext())
                .hasPreviousPage(schedulesPage.hasPrevious())
                .build();
    }

    // ========= Slot Service Methods ========
    @Override
    public Map<Date, List<SlotDto>> generateDoctorSlots(String id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundExecption(
                        "Doctor does not exist with id: " + id));

        List<ScheduleDto> scheduleDtos = getAvailableSchedulesByDoctor(id);
        Map<Date, List<SlotDto>> slotsByDate = new HashMap<>();

        int slotDurationMinutes = doctor.getAppointmentDurationInMinutes();

        // Get all booked appointments (UPCOMING or ONGOING) for this doctor
        // We need to check all dates covered by the schedules
        LocalDateTime earliestSchedule = scheduleDtos.stream()
                .map(ScheduleDto::getStartDatetime)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        
        LocalDateTime latestSchedule = scheduleDtos.stream()
                .map(ScheduleDto::getEndDatetime)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().plusDays(30));

        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndStartDatetimeBetween(id, earliestSchedule, latestSchedule)
                .stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.Upcoming || 
                              apt.getStatus() == AppointmentStatus.Ongoing)
                .toList();

        for (ScheduleDto schedule : scheduleDtos) {
            LocalDateTime currentSlotStart = schedule.getStartDatetime();
            LocalDateTime scheduleEnd = schedule.getEndDatetime();

            // Generate slots until we can't fit another full slot
            while (currentSlotStart.plusMinutes(slotDurationMinutes).isBefore(scheduleEnd) ||
                    currentSlotStart.plusMinutes(slotDurationMinutes).isEqual(scheduleEnd)) {

                LocalDateTime currentSlotEnd = currentSlotStart.plusMinutes(slotDurationMinutes);

                // Check if this slot conflicts with any booked appointment
                final LocalDateTime slotStart = currentSlotStart;
                final LocalDateTime slotEnd = currentSlotEnd;
                
                boolean isBooked = bookedAppointments.stream()
                        .anyMatch(apt -> 
                            // Slot overlaps with appointment if:
                            // 1. Slot starts before appointment ends AND
                            // 2. Slot ends after appointment starts
                            slotStart.isBefore(apt.getEndDatetime()) && 
                            slotEnd.isAfter(apt.getStartDatetime())
                        );

                // Only add slot if it's not booked
                if (!isBooked) {
                    // Create slot
                    SlotDto slot = new SlotDto();
                    slot.setStartDatetime(currentSlotStart);
                    slot.setEndDatetime(currentSlotEnd);

                    // Convert to Date for grouping (using SQL Date or java.util.Date)
                    Date date = java.sql.Date.valueOf(currentSlotStart.toLocalDate());

                    // Add to map
                    slotsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(slot);
                }

                // Move to next slot
                currentSlotStart = currentSlotEnd;
            }
        }

        return slotsByDate;
    }

}
