package Singheatlh.springboot_backend.entity;

import java.time.LocalDateTime;

import Singheatlh.springboot_backend.entity.enums.QueueStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "queue_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueueTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;
    
    @Column(name = "appointment_id", nullable = false, unique = true)
    private Long appointmentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QueueStatus status = QueueStatus.CHECKED_IN;
    
    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;
    
    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;
    
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;
    
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "is_fast_tracked")
    private Boolean isFastTracked = false;
    
    @Column(name = "fast_track_reason")
    private String fastTrackReason;
    
    // Relationship with Appointment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
    private Appointment appointment;
    
    public QueueTicket(Long appointmentId, LocalDateTime checkInTime, Integer queueNumber, 
                       Long clinicId, Long doctorId, Long patientId) {
        this.appointmentId = appointmentId;
        this.checkInTime = checkInTime;
        this.queueNumber = queueNumber;
        this.clinicId = clinicId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.status = QueueStatus.CHECKED_IN;
        this.isFastTracked = false;
    }
}
