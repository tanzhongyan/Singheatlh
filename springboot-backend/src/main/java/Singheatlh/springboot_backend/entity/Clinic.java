package Singheatlh.springboot_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "clinics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clinic_id")
    private Integer clinicId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "telephone_number", nullable = false)
    private String telephoneNumber;

    @Column(nullable = false)
    private String type;

    @Column(name = "opening_hours", nullable = false)
    private LocalTime openingHours;

    @Column(name = "closing_hours", nullable = false)
    private LocalTime closingHours;

    @Column(name = "appointment_slot_duration")
    private Integer appointmentSlotDuration;

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Doctor> doctors;
}