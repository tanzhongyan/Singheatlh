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
    private Integer ticketId;
    
    @Column(name = "appointment_id", nullable = false, unique = true, length = 10)
    private String appointmentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QueueStatus status = QueueStatus.CHECKED_IN;
    
    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;
    
    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;
    
    @Column(name = "is_fast_tracked")
    private Boolean isFastTracked = false;
    
    @Column(name = "fast_track_reason")
    private String fastTrackReason;
    
    @Column(name = "ticket_number_for_day")
    private Integer ticketNumberForDay;
    
    @Column(name = "consultation_start_time")
    private LocalDateTime consultationStartTime;
    
    @Column(name = "consultation_complete_time")
    private LocalDateTime consultationCompleteTime;
    
    // Relationship with Appointment (EAGER fetch to access related data)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
    private Appointment appointment;
    
    public QueueTicket(String appointmentId, LocalDateTime checkInTime, Integer queueNumber) {
        this.appointmentId = appointmentId;
        this.checkInTime = checkInTime;
        this.queueNumber = queueNumber;
        this.status = QueueStatus.CHECKED_IN;
        this.isFastTracked = false;
    }
    
    // Helper methods to get data from related Appointment
    public Integer getClinicId() {
        return appointment != null && appointment.getDoctor() != null ? 
            appointment.getDoctor().getClinicId() : null;
    }
    
    public String getDoctorId() {
        return appointment != null ? appointment.getDoctorId() : null;
    }
    
    public java.util.UUID getPatientId() {
        return appointment != null ? appointment.getPatientId() : null;
    }
}
