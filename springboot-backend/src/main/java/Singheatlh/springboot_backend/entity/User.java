package Singheatlh.springboot_backend.entity;

import Singheatlh.springboot_backend.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name="users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_name")
    String username;

    String name;

    @Column(unique = true)
    String email;

    @Column(name = "hashed_password")
    String hashedPassword;

    @Enumerated(EnumType.STRING)
    private Role role;
}
