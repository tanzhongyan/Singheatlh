package Singheatlh.springboot_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Singheatlh.springboot_backend.entity.QueueTicket;
import Singheatlh.springboot_backend.entity.enums.QueueStatus;

@Repository
public interface QueueTicketRepository extends JpaRepository<QueueTicket, Long> {
    
    // Find queue ticket by appointment ID (one to one)
    Optional<QueueTicket> findByAppointmentId(Long appointmentId);
    
    // Find all queue tickets for a specific patient
    List<QueueTicket> findByPatientId(Long patientId);
    
    // Find all queue tickets for a specific doctor (one to many)
    List<QueueTicket> findByDoctorId(Long doctorId);
    
    // Find all queue tickets for a specific clinic (one to many)
    List<QueueTicket> findByClinicId(Long clinicId);
    
    // Find queue tickets by status (one to many)
    List<QueueTicket> findByStatus(QueueStatus status);
    
    // Find queue tickets for a doctor with specific status
    List<QueueTicket> findByDoctorIdAndStatus(Long doctorId, QueueStatus status);
    
    // Find queue tickets for a clinic with specific status
    List<QueueTicket> findByClinicIdAndStatus(Long clinicId, QueueStatus status);
    
    // Find active queue tickets for a doctor today (ordered by queue number)
    @Query("SELECT qt FROM QueueTicket qt WHERE qt.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status NOT IN ('COMPLETED', 'NO_SHOW') " +
           "ORDER BY CASE WHEN qt.isFastTracked = true THEN 0 ELSE 1 END, qt.queueNumber ASC")
    List<QueueTicket> findActiveQueueByDoctorIdAndDate(
        @Param("doctorId") Long doctorId, 
        @Param("date") LocalDateTime date);
    
    // Find active queue tickets for a clinic today (ordered by queue number)
    @Query("SELECT qt FROM QueueTicket qt WHERE qt.clinicId = :clinicId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status NOT IN ('COMPLETED', 'NO_SHOW') " +
           "ORDER BY CASE WHEN qt.isFastTracked = true THEN 0 ELSE 1 END, qt.queueNumber ASC")
    List<QueueTicket> findActiveQueueByClinicIdAndDate(
        @Param("clinicId") Long clinicId, 
        @Param("date") LocalDateTime date);
    
    // Get the maximum queue number for a doctor on a specific date
    @Query("SELECT MAX(qt.queueNumber) FROM QueueTicket qt " +
           "WHERE qt.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date)")
    Integer findMaxQueueNumberByDoctorIdAndDate(
        @Param("doctorId") Long doctorId, 
        @Param("date") LocalDateTime date);
    
    // Find the current queue number being served (CALLED status)
    @Query("SELECT qt FROM QueueTicket qt WHERE qt.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status IN ('CALLED') " +
           "ORDER BY qt.queueNumber ASC")
    List<QueueTicket> findCurrentQueueNumberByDoctorIdAndDate(
        @Param("doctorId") Long doctorId, 
        @Param("date") LocalDateTime date);
    
    // Find queue tickets that need to be notified (3 away from current)
    @Query("SELECT qt FROM QueueTicket qt WHERE qt.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status = 'CHECKED_IN' " +
           "AND qt.queueNumber <= :targetQueueNumber " +
           "ORDER BY qt.queueNumber ASC")
    List<QueueTicket> findTicketsToNotify3Away(
        @Param("doctorId") Long doctorId, 
        @Param("date") LocalDateTime date,
        @Param("targetQueueNumber") Integer targetQueueNumber);
    
    // Find queue tickets that need to be notified (next in line)
    @Query("SELECT qt FROM QueueTicket qt WHERE qt.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status = 'CHECKED_IN' " +
           "AND qt.queueNumber = :nextQueueNumber")
    Optional<QueueTicket> findTicketToNotifyNext(
        @Param("doctorId") Long doctorId, 
        @Param("date") LocalDateTime date,
        @Param("nextQueueNumber") Integer nextQueueNumber);
    
    // Count active queue tickets for a doctor today
    @Query("SELECT COUNT(qt) FROM QueueTicket qt WHERE qt.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status NOT IN ('COMPLETED', 'NO_SHOW')")
    Long countActiveQueueByDoctorIdAndDate(
        @Param("doctorId") Long doctorId, 
        @Param("date") LocalDateTime date);
    
    // Find patient's curreent position in queue
    @Query("SELECT COUNT(qt) FROM QueueTicket qt WHERE qt.doctorId = :doctorId " +
           "AND DATE(qt.checkInTime) = DATE(:date) " +
           "AND qt.status NOT IN ('COMPLETED', 'NO_SHOW') " +
           "AND (qt.queueNumber < :queueNumber " +
           "     OR (qt.queueNumber = :queueNumber AND qt.ticketId < :ticketId)) " +
           "ORDER BY CASE WHEN qt.isFastTracked = true THEN 0 ELSE 1 END, qt.queueNumber ASC")
    Long countQueuePositionBefore(
        @Param("doctorId") Long doctorId, 
        @Param("date") LocalDateTime date,
        @Param("queueNumber") Integer queueNumber,
        @Param("ticketId") Long ticketId);
}
