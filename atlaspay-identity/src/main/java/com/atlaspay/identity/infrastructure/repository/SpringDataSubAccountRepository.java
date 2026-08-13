package com.atlaspay.identity.infrastructure.repository;

import com.atlaspay.identity.infrastructure.entity.SubAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataSubAccountRepository extends JpaRepository<SubAccountJpaEntity, Long> {
    Optional<SubAccountJpaEntity> findByIntegrationAndBankCodeAndAccountNumber(Long merchantId, String bankCode, String accountNumber);
}
