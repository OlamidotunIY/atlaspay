package com.atlaspay.admin.infrastructure.repository;

import com.atlaspay.admin.infrastructure.entity.AdminJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataAdminRepository extends JpaRepository<AdminJpaEntity, Long> {
    Optional<AdminJpaEntity> findByEmployeeCode(String employeeCode);
    boolean existsByPersonalEmail(String personalEmail);
}
