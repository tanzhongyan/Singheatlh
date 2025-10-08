package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.dto.ClinicStaffDto;
import Singheatlh.springboot_backend.entity.ClinicStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicStaffRepository extends JpaRepository<ClinicStaff, Long> {
    List<ClinicStaff> findByNameContainingIgnoreCase(String name);
}
