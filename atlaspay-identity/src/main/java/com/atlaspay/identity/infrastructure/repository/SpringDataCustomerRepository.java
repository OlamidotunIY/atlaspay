package com.atlaspay.identity.infrastructure.repository;

import com.atlaspay.identity.infrastructure.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, Long> {
    Optional<CustomerJpaEntity> findByIntegrationAndEmail(Long merchantId, String email);
}
