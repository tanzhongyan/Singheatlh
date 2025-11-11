package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.Clinic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Integer> {
    Optional<Clinic> findByName(String name);
    List<Clinic> findByType(String type);

    @Query(value = "SELECT * FROM clinic c WHERE " +
           "(:search IS NULL OR c.name ILIKE :searchPattern OR c.address ILIKE :searchPattern)",
           countQuery = "SELECT COUNT(*) FROM clinic c WHERE " +
           "(:search IS NULL OR c.name ILIKE :searchPattern OR c.address ILIKE :searchPattern)",
           nativeQuery = true)
    Page<Clinic> findWithPaginationAndSearch(
        @Param("search") String search,
        @Param("searchPattern") String searchPattern,
        Pageable pageable
    );
}
