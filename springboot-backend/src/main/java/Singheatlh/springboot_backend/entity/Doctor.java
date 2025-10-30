package Singheatlh.springboot_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @Column(name = "doctor_id", length = 10)
    private String doctorId; // Changed from Long to String to match CHAR(10)

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId; // Direct foreign key instead of relationship for now

    // Note: "schedule" column doesn't exist in schema - removed
    // Use Schedule entity for doctor availability

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", insertable = false, updatable = false)
    private Clinic clinic;

    @Column
    private Integer appointmentDurationInMinutes = 15;
}
