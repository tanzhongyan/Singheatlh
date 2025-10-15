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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;
}
