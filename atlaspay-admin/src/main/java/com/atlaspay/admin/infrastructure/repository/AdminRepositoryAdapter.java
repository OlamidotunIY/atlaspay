package com.atlaspay.admin.infrastructure.repository;

import com.atlaspay.admin.domain.model.Admin;
import com.atlaspay.admin.domain.repository.AdminRepository;
import com.atlaspay.admin.infrastructure.entity.AdminJpaEntity;
import com.atlaspay.admin.infrastructure.mapper.AdminMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminRepositoryAdapter implements AdminRepository {

    private final SpringDataAdminRepository jpaRepository;
    private final AdminMapper mapper;

    public AdminRepositoryAdapter(SpringDataAdminRepository jpaRepository, AdminMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Admin save(Admin admin) {
        AdminJpaEntity entity = mapper.toEntity(admin);
        AdminJpaEntity saved = jpaRepository.save(entity);
        admin.setId(saved.getId()); // set auto-generated ID if new
        return admin; // Return original with updated ID to preserve events
    }

    @Override
    public Optional<Admin> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Admin> findByEmployeeCode(String employeeCode) {
        return jpaRepository.findByEmployeeCode(employeeCode).map(mapper::toDomain);
    }

    @Override
    public Long nextIdentity() {
        return null; // Using IDENTITY generation strategy in JPA
    }

    @Override
    public boolean existsByPersonalEmail(String personalEmail) {
        return jpaRepository.existsByPersonalEmail(personalEmail);
    }
}
