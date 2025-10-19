package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient,UUID> {  // Changed from String to UUID
    Optional<Patient> findByEmail(String email);
}
