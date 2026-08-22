package com.atlaspay.admin.domain.repository;

import com.atlaspay.admin.domain.model.Admin;
import java.util.Optional;

public interface AdminRepository {
    Admin save(Admin admin);
    Optional<Admin> findById(Long id);
    Optional<Admin> findByEmployeeCode(String employeeCode);
    Long nextIdentity();
    boolean existsByPersonalEmail(String personalEmail);
}
