package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.Schedule;
import Singheatlh.springboot_backend.entity.enums.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    // Find upcoming available schedules for a doctor
    @Query("SELECT s FROM Schedule s WHERE s.doctorId = :doctorId " +
           "AND s.type = 'AVAILABLE' " +
           "AND s.startDatetime > :currentTime " +
           "ORDER BY s.startDatetime ASC")
    List<Schedule> findUpcomingAvailableSchedules(
            @Param("doctorId") String doctorId,
            @Param("currentTime") LocalDateTime currentTime
    );
}
