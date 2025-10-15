package Singheatlh.springboot_backend.entity;

import Singheatlh.springboot_backend.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public class User {
    @Id
    @Column(name = "supabase_uid", unique = true) // NEW: Store Supabase user ID
    String supabaseUid;

    @Column(name = "user_name")
    String username;

    String name;

    @Column(unique = true)
    String email;

    @Enumerated(EnumType.STRING)
    private Role role;
}
