package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Integer> {
    Optional<Clinic> findByName(String name);
    List<Clinic> findByType(String type);
}
