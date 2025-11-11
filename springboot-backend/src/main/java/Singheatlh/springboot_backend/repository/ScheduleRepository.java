package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.Schedule;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {

    // Find all schedules for a specific doctor
    List<Schedule> findByDoctorId(String doctorId);

    // Find schedules by type (AVAILABLE or UNAVAILABLE)
    List<Schedule> findByType(ScheduleType type);

    // Find schedules for a doctor by type
    List<Schedule> findByDoctorIdAndType(String doctorId, ScheduleType type);

    // Find schedules within a date range
    @Query("SELECT s FROM Schedule s WHERE s.startDatetime >= :startDate AND s.endDatetime <= :endDate ORDER BY s.startDatetime ASC")
    List<Schedule> findSchedulesInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Find available schedules for a doctor within a date range
    @Query("SELECT s FROM Schedule s WHERE s.doctorId = :doctorId " +
           "AND s.type = 'AVAILABLE' " +
           "AND s.startDatetime >= :startDate " +
           "AND s.endDatetime <= :endDate " +
           "ORDER BY s.startDatetime ASC")
    List<Schedule> findAvailableSchedulesByDoctorAndDateRange(
            @Param("doctorId") String doctorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Check if doctor has any schedule at a specific time (to prevent overlapping)
    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.doctorId = :doctorId " +
           "AND ((s.startDatetime <= :startTime AND s.endDatetime > :startTime) " +
           "OR (s.startDatetime < :endTime AND s.endDatetime >= :endTime) " +
           "OR (s.startDatetime >= :startTime AND s.endDatetime <= :endTime))")
    boolean existsOverlappingSchedule(
            @Param("doctorId") String doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Check for overlapping schedules excluding a specific schedule (for updates)
    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.doctorId = :doctorId " +
           "AND s.scheduleId != :excludeScheduleId " +
           "AND ((s.startDatetime <= :startTime AND s.endDatetime > :startTime) " +
           "OR (s.startDatetime < :endTime AND s.endDatetime >= :endTime) " +
           "OR (s.startDatetime >= :startTime AND s.endDatetime <= :endTime))")
    boolean existsOverlappingScheduleExcluding(
            @Param("doctorId") String doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeScheduleId") String excludeScheduleId
    );

    // Find upcoming available schedules for a doctor
    @Query("SELECT s FROM Schedule s WHERE s.doctorId = :doctorId " +
           "AND s.type = 'AVAILABLE' " +
           "AND s.startDatetime > :currentTime " +
           "ORDER BY s.startDatetime ASC")
    List<Schedule> findUpcomingAvailableSchedules(
            @Param("doctorId") String doctorId,
            @Param("currentTime") LocalDateTime currentTime
    );

    // Find schedules for a doctor with pagination
    @Query(value = "SELECT * FROM schedule s WHERE s.doctor_id = :doctorId ORDER BY s.start_datetime ASC",
           countQuery = "SELECT COUNT(*) FROM schedule s WHERE s.doctor_id = :doctorId",
           nativeQuery = true)
    Page<Schedule> findByDoctorIdPaginated(
            @Param("doctorId") String doctorId,
            Pageable pageable
    );

    // Find schedules for a doctor within a date range with pagination
    @Query(value = "SELECT * FROM schedule s WHERE s.doctor_id = :doctorId " +
           "AND s.start_datetime >= :startDate AND s.end_datetime <= :endDate " +
           "ORDER BY s.start_datetime ASC",
           countQuery = "SELECT COUNT(*) FROM schedule s WHERE s.doctor_id = :doctorId " +
           "AND s.start_datetime >= :startDate AND s.end_datetime <= :endDate",
           nativeQuery = true)
    Page<Schedule> findByDoctorIdAndDateRangePaginated(
            @Param("doctorId") String doctorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Get maximum schedule ID for efficient ID generation
    @Query(value = "SELECT s.schedule_id FROM schedule s ORDER BY s.schedule_id DESC LIMIT 1",
           nativeQuery = true)
    Optional<String> findMaxScheduleId();
}
