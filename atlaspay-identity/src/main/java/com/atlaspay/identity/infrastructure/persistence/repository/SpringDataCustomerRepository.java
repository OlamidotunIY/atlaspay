package com.atlaspay.identity.infrastructure.persistence.repository;

import com.atlaspay.identity.infrastructure.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, String> {
    Optional<CustomerJpaEntity> findByMerchantIdAndEmail(String merchantId, String email);
}
