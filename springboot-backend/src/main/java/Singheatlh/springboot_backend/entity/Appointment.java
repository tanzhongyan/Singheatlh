package Singheatlh.springboot_backend.entity;

import java.time.LocalDateTime;

import Singheatlh.springboot_backend.entity.enums.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    
    @Id
    @Column(name = "appointment_id", length = 10)
    private String appointmentId; // Changed from Long to String to match CHAR(10)
    
    @Column(name = "patient_id", nullable = false)
    private java.util.UUID patientId; // Changed to UUID to match User_Profile
    
    @Column(name = "doctor_id", nullable = false, length = 10)
    private String doctorId; // Changed to String to match Doctor
    
    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime; // Renamed from appointmentDatetime
    
    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime; // Added to match schema
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private AppointmentStatus status; // Changed to enum for type safety

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient; // Relationship to Patient entity
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Doctor doctor;
    
    public Appointment(String appointmentId, java.util.UUID patientId, String doctorId, LocalDateTime startDatetime, LocalDateTime endDatetime) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.status = AppointmentStatus.Upcoming;
    }
}
