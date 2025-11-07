package Singheatlh.springboot_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import Singheatlh.springboot_backend.entity.QueueTicket;
import Singheatlh.springboot_backend.entity.enums.QueueStatus;

@Repository
public interface QueueTicketRepository extends JpaRepository<QueueTicket, Integer> {
    
    // Find queue ticket by ID with appointment eagerly loaded
    @Query("SELECT qt FROM QueueTicket qt LEFT JOIN FETCH qt.appointment WHERE qt.ticketId = :ticketId")
    Optional<QueueTicket> findByIdWithAppointment(@Param("ticketId") Integer ticketId);
    
    // Find queue ticket by appointment ID (one to one)
    Optional<QueueTicket> findByAppointmentId(String appointmentId);
    
    // Find all queue tickets for a specific patient (via JOIN with Appointment)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a WHERE a.patientId = :patientId")
    List<QueueTicket> findByPatientId(@Param("patientId") java.util.UUID patientId);
    
    // Find all queue tickets for a specific doctor (via JOIN with Appointment)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a WHERE a.doctorId = :doctorId")
    List<QueueTicket> findByDoctorId(@Param("doctorId") String doctorId);
    
    // Find all queue tickets for a specific clinic (via JOIN with Appointment and Doctor)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a JOIN a.doctor d WHERE d.clinicId = :clinicId")
    List<QueueTicket> findByClinicId(@Param("clinicId") Integer clinicId);
    
    // Find queue tickets by status (one to many)
    List<QueueTicket> findByStatus(QueueStatus status);
    
    // Find queue tickets for a doctor with specific status (via JOIN with Appointment)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a WHERE a.doctorId = :doctorId AND qt.status = :status")
    List<QueueTicket> findByDoctorIdAndStatus(@Param("doctorId") String doctorId, @Param("status") QueueStatus status);
    
    // Find queue tickets for a clinic with specific status (via JOIN with Appointment and Doctor)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a JOIN a.doctor d WHERE d.clinicId = :clinicId AND qt.status = :status")
    List<QueueTicket> findByClinicIdAndStatus(@Param("clinicId") Integer clinicId, @Param("status") QueueStatus status);
    
    // Find active queue tickets for a doctor today (ordered by queue number)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a WHERE a.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status NOT IN ('COMPLETED', 'NO_SHOW') " +
           "ORDER BY qt.queueNumber ASC")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<QueueTicket> findActiveQueueByDoctorIdAndDate(
        @Param("doctorId") String doctorId, 
        @Param("date") LocalDateTime date);
    
    // Find active queue tickets for a clinic today (ordered by queue number)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a JOIN a.doctor d WHERE d.clinicId = :clinicId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status NOT IN ('COMPLETED', 'NO_SHOW') " +
           "ORDER BY qt.queueNumber ASC")
    List<QueueTicket> findActiveQueueByClinicIdAndDate(
        @Param("clinicId") Integer clinicId, 
        @Param("date") LocalDateTime date);
    
    // Get the maximum queue number for a doctor on a specific date (excluding completed patients)
    @Query("SELECT MAX(qt.queueNumber) FROM QueueTicket qt JOIN qt.appointment a " +
           "WHERE a.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.queueNumber > 0")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Integer findMaxQueueNumberByDoctorIdAndDate(
        @Param("doctorId") String doctorId, 
        @Param("date") LocalDateTime date);
    
    // Find the current queue number being served (CALLED status)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a WHERE a.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status IN ('CALLED') " +
           "ORDER BY qt.queueNumber ASC")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<QueueTicket> findCurrentQueueNumberByDoctorIdAndDate(
        @Param("doctorId") String doctorId, 
        @Param("date") LocalDateTime date);
    
    // Find queue ticket that needs to be notified (exactly 3 away from current)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a WHERE a.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status = 'CHECKED_IN' " +
           "AND qt.queueNumber = :targetQueueNumber")
    Optional<QueueTicket> findTicketToNotify3Away(
        @Param("doctorId") String doctorId, 
        @Param("date") LocalDateTime date,
        @Param("targetQueueNumber") Integer targetQueueNumber);
    
    // Find queue tickets that need to be notified (next in line)
    @Query("SELECT qt FROM QueueTicket qt JOIN qt.appointment a WHERE a.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status = 'CHECKED_IN' " +
           "AND qt.queueNumber = :nextQueueNumber")
    Optional<QueueTicket> findTicketToNotifyNext(
        @Param("doctorId") String doctorId, 
        @Param("date") LocalDateTime date,
        @Param("nextQueueNumber") Integer nextQueueNumber);
    
    // Count active queue tickets for a doctor today
    @Query("SELECT COUNT(qt) FROM QueueTicket qt JOIN qt.appointment a WHERE a.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status NOT IN ('COMPLETED', 'NO_SHOW')")
    Long countActiveQueueByDoctorIdAndDate(
        @Param("doctorId") String doctorId, 
        @Param("date") LocalDateTime date);
    
    // Find patient's current position in queue
    @Query("SELECT COUNT(qt) FROM QueueTicket qt JOIN qt.appointment a WHERE a.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status NOT IN ('COMPLETED', 'NO_SHOW') " +
           "AND (qt.queueNumber < :queueNumber " +
           "     OR (qt.queueNumber = :queueNumber AND qt.ticketId < :ticketId)) " +
           "ORDER BY qt.queueNumber ASC")
    Long countQueuePositionBefore(
        @Param("doctorId") String doctorId, 
        @Param("date") LocalDateTime date,
        @Param("queueNumber") Integer queueNumber,
        @Param("ticketId") Integer ticketId);
}
