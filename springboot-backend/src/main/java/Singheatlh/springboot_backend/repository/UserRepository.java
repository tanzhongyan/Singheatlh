package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.User;
import Singheatlh.springboot_backend.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
}
