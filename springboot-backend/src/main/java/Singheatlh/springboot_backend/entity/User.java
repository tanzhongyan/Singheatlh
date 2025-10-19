package Singheatlh.springboot_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import Singheatlh.springboot_backend.entity.enums.Role;

@NoArgsConstructor
@Data
@Entity
@Table(name = "user_profile")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public class User {
    @Id
    @Column(name = "user_id")
    private java.util.UUID userId;

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "telephone_number")
    private String telephoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 1, insertable = false, updatable = false)
    private Role role;

    @Column(name = "clinic_id")
    private Integer clinicId;
}
