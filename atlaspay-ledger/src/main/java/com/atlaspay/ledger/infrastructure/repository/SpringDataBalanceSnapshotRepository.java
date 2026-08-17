package com.atlaspay.ledger.infrastructure.repository;

import com.atlaspay.ledger.infrastructure.entity.BalanceSnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface SpringDataBalanceSnapshotRepository extends JpaRepository<BalanceSnapshotJpaEntity, Long> {
    @Query("SELECT e FROM BalanceSnapshotJpaEntity e WHERE e.accountId = :accountId ORDER BY e.createdAt DESC LIMIT 1")
    Optional<BalanceSnapshotJpaEntity> findLatestSnapshot(@Param("accountId") Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM BalanceSnapshotJpaEntity e WHERE e.accountId = :accountId ORDER BY e.createdAt DESC LIMIT 1")
    Optional<BalanceSnapshotJpaEntity> findLatestSnapshotForUpdate(@Param("accountId") Long accountId);
}
