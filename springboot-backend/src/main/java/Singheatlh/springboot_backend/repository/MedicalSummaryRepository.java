package Singheatlh.springboot_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Singheatlh.springboot_backend.entity.MedicalSummary;

@Repository
public interface MedicalSummaryRepository extends JpaRepository<MedicalSummary, String> {
    Optional<MedicalSummary> findByAppointmentId(String appointmentId);
}
