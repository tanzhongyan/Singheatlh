package Singheatlh.springboot_backend.repository;

import Singheatlh.springboot_backend.entity.User;
import Singheatlh.springboot_backend.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);

    @Query(value = "SELECT * FROM user_profile u WHERE " +
           "(:search IS NULL OR u.name ILIKE :searchPattern OR u.email ILIKE :searchPattern) AND " +
           "(:role IS NULL OR u.role = :role)",
           countQuery = "SELECT COUNT(*) FROM user_profile u WHERE " +
           "(:search IS NULL OR u.name ILIKE :searchPattern OR u.email ILIKE :searchPattern) AND " +
           "(:role IS NULL OR u.role = :role)",
           nativeQuery = true)
    Page<User> findWithPaginationAndSearch(
        @Param("search") String search,
        @Param("searchPattern") String searchPattern,
        @Param("role") String role,
        Pageable pageable
    );
}
