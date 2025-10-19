package Singheatlh.springboot_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {  // Changed to String (CHAR(10))
    
    // Find all appointments for a specific patient
    List<Appointment> findByPatientId(UUID patientId);  // Changed to UUID
    
    // Find all appointments for a specific doctor
    List<Appointment> findByDoctorId(String doctorId);  // Changed to String
    
    // Find appointments by status
    List<Appointment> findByStatus(AppointmentStatus status);  // Changed to enum
    
    // Find appointments for a patient with specific status
    List<Appointment> findByPatientIdAndStatus(UUID patientId, AppointmentStatus status);
    
    // Find appointments for a doctor with specific status
    List<Appointment> findByDoctorIdAndStatus(String doctorId, AppointmentStatus status);
    
    // Find appointments within a date range
    List<Appointment> findByStartDatetimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find appointments for a doctor within a date range (useful for checking conflicts)
    List<Appointment> findByDoctorIdAndStartDatetimeBetween(
        String doctorId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find upcoming appointments for a patient
    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId " +
           "AND a.startDatetime > :currentTime " +
           "AND a.status = 'Upcoming' " +
           "ORDER BY a.startDatetime ASC")
    List<Appointment> findUpcomingAppointmentsByPatientId(
        @Param("patientId") UUID patientId, 
        @Param("currentTime") LocalDateTime currentTime);
    
    // Find upcoming appointments for a doctor
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId " +
           "AND a.startDatetime > :currentTime " +
           "AND a.status = 'Upcoming' " +
           "ORDER BY a.startDatetime ASC")
    List<Appointment> findUpcomingAppointmentsByDoctorId(
        @Param("doctorId") String doctorId, 
        @Param("currentTime") LocalDateTime currentTime);
    
    // Find appointments for today for a doctor
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId " +
           "AND DATE(a.startDatetime) = DATE(:date) " +
           "ORDER BY a.startDatetime ASC")
    List<Appointment> findTodayAppointmentsByDoctorId(
        @Param("doctorId") String doctorId, 
        @Param("date") LocalDateTime date);
}
