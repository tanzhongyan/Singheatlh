package Singheatlh.springboot_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("CLINIC_STAFF")
@Getter
@Setter
public class ClinicStaff extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = true)
    private Clinic clinic;
}
