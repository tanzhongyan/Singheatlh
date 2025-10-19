package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, String> {  // Changed from Long to String
    List<Doctor> findByClinicId(Integer clinicId);  // Simplified
}
