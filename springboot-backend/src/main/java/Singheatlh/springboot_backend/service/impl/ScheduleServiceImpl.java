package Singheatlh.springboot_backend.service.impl;

import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.entity.Schedule;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;
import Singheatlh.springboot_backend.exception.ResourceNotFoundExecption;
import Singheatlh.springboot_backend.mapper.ScheduleMapper;
import Singheatlh.springboot_backend.repository.DoctorRepository;
import Singheatlh.springboot_backend.repository.ScheduleRepository;
import Singheatlh.springboot_backend.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final ScheduleMapper scheduleMapper;

    @Override
    public ScheduleDto getById(String id) {
        Schedule schedule = scheduleRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundExecption("Schedule does not exist with the given id " + id)
        );
        return scheduleMapper.toDto(schedule);
    }

    @Override
    @Transactional
    public ScheduleDto createSchedule(ScheduleDto scheduleDto) {
        // 1. Validate doctor exists
        doctorRepository.findById(scheduleDto.getDoctorId()).orElseThrow(
                () -> new ResourceNotFoundExecption("Doctor does not exist with id " + scheduleDto.getDoctorId())
        );

        // 2. Validate time range
        if (scheduleDto.getEndDatetime().isBefore(scheduleDto.getStartDatetime()) ||
            scheduleDto.getEndDatetime().isEqual(scheduleDto.getStartDatetime())) {
            throw new IllegalArgumentException("End datetime must be after start datetime");
        }

        // 3. Check for overlapping schedules
        if (scheduleRepository.existsOverlappingSchedule(
                scheduleDto.getDoctorId(),
                scheduleDto.getStartDatetime(),
                scheduleDto.getEndDatetime())) {
            throw new IllegalArgumentException(
                    "Doctor already has a schedule that overlaps with this time period"
            );
        }

        // 4. Convert DTO to Entity
        Schedule schedule = scheduleMapper.toEntity(scheduleDto);

        // 5. Save and return
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return scheduleMapper.toDto(savedSchedule);
    }

    @Override
    public List<ScheduleDto> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAll();
        return schedules.stream()
                .map(scheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleDto updateSchedule(ScheduleDto scheduleDto) {
        // 1. Find existing schedule
        Schedule schedule = scheduleRepository.findById(scheduleDto.getScheduleId()).orElseThrow(
                () -> new ResourceNotFoundExecption("Schedule does not exist with id " + scheduleDto.getScheduleId())
        );

        // 2. Validate time range
        if (scheduleDto.getEndDatetime().isBefore(scheduleDto.getStartDatetime()) ||
            scheduleDto.getEndDatetime().isEqual(scheduleDto.getStartDatetime())) {
            throw new IllegalArgumentException("End datetime must be after start datetime");
        }

        // 3. Update fields
        schedule.setStartDatetime(scheduleDto.getStartDatetime());
        schedule.setEndDatetime(scheduleDto.getEndDatetime());
        schedule.setType(scheduleDto.getType());

        // 4. Save and return
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return scheduleMapper.toDto(savedSchedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(String id) {
        scheduleRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundExecption("Schedule does not exist with id " + id)
        );
        scheduleRepository.deleteById(id);
    }

    @Override
    public List<ScheduleDto> getSchedulesByDoctorId(String doctorId) {
        List<Schedule> schedules = scheduleRepository.findByDoctorId(doctorId);
        return schedules.stream()
                .map(scheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getSchedulesByType(ScheduleType type) {
        List<Schedule> schedules = scheduleRepository.findByType(type);
        return schedules.stream()
                .map(scheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getAvailableSchedulesByDoctor(String doctorId) {
        List<Schedule> schedules = scheduleRepository.findByDoctorIdAndType(doctorId, ScheduleType.AVAILABLE);
        return schedules.stream()
                .map(scheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getSchedulesInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Schedule> schedules = scheduleRepository.findSchedulesInDateRange(startDate, endDate);
        return schedules.stream()
                .map(scheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getAvailableSchedulesByDoctorAndDateRange(
            String doctorId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Schedule> schedules = scheduleRepository.findAvailableSchedulesByDoctorAndDateRange(
                doctorId, startDate, endDate
        );
        return schedules.stream()
                .map(scheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDto> getUpcomingAvailableSchedules(String doctorId) {
        List<Schedule> schedules = scheduleRepository.findUpcomingAvailableSchedules(
                doctorId, LocalDateTime.now()
        );
        return schedules.stream()
                .map(scheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasOverlappingSchedule(String doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        return scheduleRepository.existsOverlappingSchedule(doctorId, startTime, endTime);
    }
}
