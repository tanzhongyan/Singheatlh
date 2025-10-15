package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.entity.ClinicStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClinicStaffRepository extends JpaRepository<ClinicStaff, String> {
    List<ClinicStaff> findByNameContainingIgnoreCase(String name);

    List<ClinicStaff> findByClinicClinicId(Integer clinicId);

    Optional<ClinicStaff> findByEmail(String email);
}
