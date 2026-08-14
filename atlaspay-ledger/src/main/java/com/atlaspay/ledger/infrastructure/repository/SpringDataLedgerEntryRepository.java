package com.atlaspay.ledger.infrastructure.repository;

import com.atlaspay.ledger.infrastructure.entity.LedgerEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataLedgerEntryRepository extends JpaRepository<LedgerEntryJpaEntity, Long> {

    @Query("SELECT e FROM LedgerEntryJpaEntity e WHERE e.accountId = :accountId AND e.id > :lastEntryId ORDER BY e.id ASC")
    List<LedgerEntryJpaEntity> findEntriesAfter(@Param("accountId") Long accountId, @Param("lastEntryId") Long lastEntryId);

    List<LedgerEntryJpaEntity> findByAccountId(Long accountId);
}
