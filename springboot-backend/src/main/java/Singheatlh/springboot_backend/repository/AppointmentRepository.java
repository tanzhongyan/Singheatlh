package Singheatlh.springboot_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Singheatlh.springboot_backend.entity.Appointment;
import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Find all appointments for a specific patient
    List<Appointment> findByPatientId(Long patientId);
    
    // Find all appointments for a specific doctor
    List<Appointment> findByDoctorId(Long doctorId);
    
    // Find all appointments for a specific clinic
    List<Appointment> findByClinicId(Long clinicId);
    
    // Find appointments by status
    List<Appointment> findByStatus(AppointmentStatus status);
    
    // Find appointments for a patient with specific status
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);
    
    // Find appointments for a doctor with specific status
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    
    // Find appointments within a date range
    List<Appointment> findByAppointmentDatetimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find appointments for a doctor within a date range (useful for checking conflicts)
    List<Appointment> findByDoctorIdAndAppointmentDatetimeBetween(
        Long doctorId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find upcoming appointments for a patient
    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId " +
           "AND a.appointmentDatetime > :currentTime " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY a.appointmentDatetime ASC")
    List<Appointment> findUpcomingAppointmentsByPatientId(
        @Param("patientId") Long patientId, 
        @Param("currentTime") LocalDateTime currentTime);
    
    // Find upcoming appointments for a doctor
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId " +
           "AND a.appointmentDatetime > :currentTime " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "ORDER BY a.appointmentDatetime ASC")
    List<Appointment> findUpcomingAppointmentsByDoctorId(
        @Param("doctorId") Long doctorId, 
        @Param("currentTime") LocalDateTime currentTime);
    
    // Find appointments for today for a doctor
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId " +
           "AND DATE(a.appointmentDatetime) = DATE(:date) " +
           "ORDER BY a.appointmentDatetime ASC")
    List<Appointment> findTodayAppointmentsByDoctorId(
        @Param("doctorId") Long doctorId, 
        @Param("date") LocalDateTime date);
}
