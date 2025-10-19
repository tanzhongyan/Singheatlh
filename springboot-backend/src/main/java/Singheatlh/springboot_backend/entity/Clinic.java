package Singheatlh.springboot_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "clinic")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clinic_id")
    private Integer clinicId;

    @Column(name = "type", nullable = false, length = 1)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "telephone_number")
    private String telephoneNumber;

    @Column(name = "opening_hours")
    private LocalTime openingHours;

    @Column(name = "closing_hours")
    private LocalTime closingHours;


    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Doctor> doctors;
}