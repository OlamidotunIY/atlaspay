package com.atlaspay.ledger.infrastructure.repository;

import com.atlaspay.ledger.infrastructure.entity.LedgerTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataLedgerTransactionRepository extends JpaRepository<LedgerTransactionJpaEntity, Long> {
    @Query("SELECT COUNT(e) > 0 FROM LedgerTransactionJpaEntity e WHERE e.transactionId = :transactionId AND e.sourceSystem = :sourceSystem")
    boolean existsBySourceTransaction(@Param("transactionId") String transactionId, @Param("sourceSystem") String sourceSystem);
}
