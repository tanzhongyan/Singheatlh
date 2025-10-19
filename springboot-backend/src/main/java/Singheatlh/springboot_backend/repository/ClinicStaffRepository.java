package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.ClinicStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClinicStaffRepository extends JpaRepository<ClinicStaff, UUID> {  // Changed from String to UUID
    List<ClinicStaff> findByNameContainingIgnoreCase(String name);

    List<ClinicStaff> findByClinicId(Integer clinicId);  // Simplified

    Optional<ClinicStaff> findByEmail(String email);
}
