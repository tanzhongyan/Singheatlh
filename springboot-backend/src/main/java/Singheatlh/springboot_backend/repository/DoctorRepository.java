package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, String> {  // Changed from Long to String
    List<Doctor> findByClinicId(Integer clinicId);  // Simplified

    @Query(value = "SELECT d.* FROM doctor d LEFT JOIN clinic c ON d.clinic_id = c.clinic_id WHERE " +
           "(:search IS NULL OR d.name ILIKE :searchPattern OR c.name ILIKE :searchPattern)",
           countQuery = "SELECT COUNT(*) FROM doctor d LEFT JOIN clinic c ON d.clinic_id = c.clinic_id WHERE " +
           "(:search IS NULL OR d.name ILIKE :searchPattern OR c.name ILIKE :searchPattern)",
           nativeQuery = true)
    Page<Doctor> findWithPaginationAndSearch(
        @Param("search") String search,
        @Param("searchPattern") String searchPattern,
        Pageable pageable
    );
}
