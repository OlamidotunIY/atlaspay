package com.atlaspay.identity.infrastructure.repository;

import com.atlaspay.identity.infrastructure.entity.MerchantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataMerchantRepository extends JpaRepository<MerchantJpaEntity, String> {
    Optional<MerchantJpaEntity> findByEmail(String email);
}
