package com.atlaspay.ledger.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "balance_snapshots", indexes = {
        @Index(name = "idx_balances_account_created", columnList = "account_id, created_at DESC")
})
@AllArgsConstructor()
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceSnapshotJpaEntity {

    @Id
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "last_ledger_entry_id", nullable = false)
    private Long lastLedgerEntryId;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

}
