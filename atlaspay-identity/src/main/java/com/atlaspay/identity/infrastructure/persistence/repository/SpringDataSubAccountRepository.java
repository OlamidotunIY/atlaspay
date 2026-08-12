package com.atlaspay.identity.infrastructure.persistence.repository;

import com.atlaspay.identity.infrastructure.persistence.entity.SubAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataSubAccountRepository extends JpaRepository<SubAccountJpaEntity, String> {
    Optional<SubAccountJpaEntity> findByMerchantIdAndBankCodeAndAccountNumber(String merchantId, String bankCode, String accountNumber);
}
