package com.atlaspay.ledger.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Entity
@Table(name = "ledger_transactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"transaction_id", "source_system"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerTransactionJpaEntity {

    @Id
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "posted_at", nullable = false)
    private ZonedDateTime postedAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LedgerEntryJpaEntity> entries = new ArrayList<>();

    public LedgerTransactionJpaEntity(Long id, String transactionId, String sourceSystem, ZonedDateTime postedAt, List<LedgerEntryJpaEntity> entries) {
        this.id = id;
        this.transactionId = transactionId;
        this.sourceSystem = sourceSystem;
        this.postedAt = postedAt;
        if (entries != null) {
            this.entries = entries;
            for (LedgerEntryJpaEntity entry : entries) {
                entry.setTransaction(this);
            }
        }
    }
}
