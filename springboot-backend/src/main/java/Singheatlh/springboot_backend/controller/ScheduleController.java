package Singheatlh.springboot_backend.controller;

import Singheatlh.springboot_backend.dto.PaginatedResponse;
import Singheatlh.springboot_backend.dto.ScheduleDto;
import Singheatlh.springboot_backend.dto.SlotDto;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;
import Singheatlh.springboot_backend.service.ScheduleService;
import Singheatlh.springboot_backend.validation.ScheduleOverlapValidationRule.ScheduleOverlapException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // CRUD endpoints

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable("id") String scheduleId) {
        ScheduleDto scheduleDto = scheduleService.getById(scheduleId);
        return ResponseEntity.ok(scheduleDto);
    }

    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getAllSchedules() {
        List<ScheduleDto> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @PostMapping
    public ResponseEntity<ScheduleDto> createSchedule(@Valid @RequestBody ScheduleDto scheduleDto) {
        ScheduleDto newSchedule = scheduleService.createSchedule(scheduleDto);
        return new ResponseEntity<>(newSchedule, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleDto> updateSchedule(
            @PathVariable("id") String scheduleId,
            @Valid @RequestBody ScheduleDto scheduleDto) {
        scheduleDto.setScheduleId(scheduleId);
        ScheduleDto updatedSchedule = scheduleService.updateSchedule(scheduleDto);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSchedule(@PathVariable("id") String scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok("Schedule deleted successfully!");
    }

    // Custom query endpoints

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByDoctor(@PathVariable("doctorId") String doctorId) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByDoctorId(doctorId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/doctor/{doctorId}/paginated")
    public ResponseEntity<Object> getSchedulesByDoctorPaginated(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(scheduleService.getSchedulesWithPaginationByDoctorId(doctorId, page, pageSize));
    }

    @GetMapping("/doctor/{doctorId}/paginated/date-range")
    public ResponseEntity<Object> getSchedulesByDoctorAndDateRangePaginated(
            @PathVariable String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(scheduleService.getSchedulesWithPaginationByDoctorAndDateRange(doctorId, startDate, endDate, page, pageSize));
    }

    @GetMapping("/doctor/{doctorId}/available")
    public ResponseEntity<List<ScheduleDto>> getAvailableSchedulesByDoctor(@PathVariable("doctorId") String doctorId) {
        List<ScheduleDto> schedules = scheduleService.getAvailableSchedulesByDoctor(doctorId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/doctor/{doctorId}/upcoming")
    public ResponseEntity<List<ScheduleDto>> getUpcomingAvailableSchedules(@PathVariable("doctorId") String doctorId) {
        List<ScheduleDto> schedules = scheduleService.getUpcomingAvailableSchedules(doctorId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByType(@PathVariable("type") ScheduleType type) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByType(type);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ScheduleDto>> getSchedulesInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesInDateRange(startDate, endDate);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/doctor/{doctorId}/date-range")
    public ResponseEntity<List<ScheduleDto>> getAvailableSchedulesByDoctorAndDateRange(
            @PathVariable("doctorId") String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ScheduleDto> schedules = scheduleService.getAvailableSchedulesByDoctorAndDateRange(
                doctorId, startDate, endDate
        );
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/doctor/{doctorId}/check-overlap")
    public ResponseEntity<Boolean> checkOverlappingSchedule(
            @PathVariable("doctorId") String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        boolean hasOverlap = scheduleService.hasOverlappingSchedule(doctorId, startTime, endTime);
        return ResponseEntity.ok(hasOverlap);
    }

    @GetMapping("/doctor/{doctorId}/slot")
    public ResponseEntity<Map<Date, List<SlotDto>>> getSlotsByDoctor(@PathVariable String doctorId) {
        Map<Date, List<SlotDto>> slotMap = scheduleService.generateDoctorSlots(doctorId);
        return ResponseEntity.ok(slotMap);
    }

    // Exception Handlers

    @ExceptionHandler(ScheduleOverlapException.class)
    public ResponseEntity<Map<String, String>> handleScheduleOverlapException(
            ScheduleOverlapException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Schedule Conflict");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
