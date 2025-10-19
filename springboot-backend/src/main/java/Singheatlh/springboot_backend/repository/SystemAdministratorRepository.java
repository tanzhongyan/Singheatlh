package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.SystemAdministrator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SystemAdministratorRepository extends JpaRepository<SystemAdministrator, UUID> {  // Changed from String to UUID
}
