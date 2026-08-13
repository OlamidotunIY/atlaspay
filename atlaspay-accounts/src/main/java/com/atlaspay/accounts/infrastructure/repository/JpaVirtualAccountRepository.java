package com.atlaspay.accounts.infrastructure.repository;

import com.atlaspay.accounts.infrastructure.entity.VirtualAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaVirtualAccountRepository extends JpaRepository<VirtualAccountEntity, Long> {
    List<VirtualAccountEntity> findByIntegration(Long integration);
    Optional<VirtualAccountEntity> findByNuban(String nuban);
    boolean existsByNuban(String nuban);
}
