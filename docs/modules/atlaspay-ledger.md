# Module Design Document — `atlaspay-ledger`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview
The `atlaspay-ledger` is the most critical and rigorous module in the system. It is a strictly append-only, double-entry bookkeeping system. It acts as the ultimate source of truth for all financial balances. No balances are mutated directly; they are always derived dynamically (or cached via snapshots) from the immutable journal entries.

Every financial movement (Charge, Transfer, Refund, Settlement) invokes the Ledger to write a balanced pair (or set) of `LedgerEntry` records.

---

## 2. Folder Structure

```text
atlaspay-ledger/
└── src/main/java/com/atlaspay/ledger/
    ├── domain/
    │   ├── model/
    │   │   ├── LedgerEntry.java                 # Immutable Record (Aggregate)
    │   │   ├── EntryType.java                   # Enum (CREDIT, DEBIT)
    │   │   ├── WalletId.java                    # Value Object
    │   │   ├── TransactionReference.java        # Value Object
    │   │   └── BalanceSnapshot.java             # Read Projection Record
    │   ├── exception/
    │   │   ├── LedgerErrorCode.java
    │   │   └── InsufficientFundsException.java
    │   └── repository/
    │       ├── LedgerEntryDomainRepository.java
    │       └── BalanceSnapshotDomainRepository.java
    ├── application/
    │   ├── command/
    │   │   └── PostJournalEntryCommand.java     # Enforces Double-Entry
    │   ├── query/
    │   │   ├── GetWalletBalanceQuery.java
    │   │   └── GetLedgerHistoryQuery.java
    │   ├── dto/
    │   │   ├── WalletBalanceDto.java
    │   │   └── LedgerEntryDto.java
    │   └── usecase/
    │       ├── PostJournalEntryUseCase.java
    │       └── GetWalletBalanceUseCase.java
    ├── infrastructure/
    │   ├── persistence/
    │   │   ├── LedgerEntryEntity.java
    │   │   ├── BalanceSnapshotEntity.java       # DB materialized view / cache table
    │   │   └── LedgerJpaRepository.java         # Contains Pessimistic Locks
    │   └── adapter/
    │       └── LedgerPersistenceAdapter.java
    └── presentation/rest/
        └── LedgerController.java                # Internal APIs for balances
```

---

## 3. Domain Layer

### 3.1 Aggregates & Entities

**`LedgerEntry`** (Immutable Record, not a JPA `@Entity` with setters)
```java
public record LedgerEntry(
    String id,
    WalletId walletId,
    Money amount,
    EntryType type, // CREDIT or DEBIT
    TransactionReference reference,
    String idempotencyKey,
    Instant createdAt
) {
    public LedgerEntry {
        if (amount.isNegative() || amount.isZero()) {
            throw new BusinessRuleException(LedgerErrorCode.INVALID_AMOUNT);
        }
    }
}
```

**Double-Entry Invariant Check:**
Before any entries are persisted, the domain service enforces that `SUM(CREDIT) == SUM(DEBIT)` for the batch of entries being posted.

---

## 4. Application Layer & Algorithms

### 4.1 Use Case: `PostJournalEntryUseCase`
1. Receives `PostJournalEntryCommand(List<LedgerEntryRequest> entries)`.
2. **Double-Entry Validation:** Calculates total credits and total debits. If they do not match exactly, throws `LedgerErrorCode.IMBALANCED_JOURNAL`.
3. **Idempotency Check:** Relies on the database `UNIQUE(idempotency_key)` constraint. The repository wraps `DataIntegrityViolationException` into a handled `IdempotencyException`.
4. **Balance Validation (Pessimistic Locking):**
    *   For the wallet(s) being debited, queries the `BalanceSnapshotEntity` using `SELECT ... FOR UPDATE` (via `LockModeType.PESSIMISTIC_WRITE` in Spring Data JPA).
    *   Verifies `available_balance >= debit_amount`. Throws `InsufficientFundsException` if it fails.
5. **Persistence:**
    *   Inserts the new `LedgerEntryEntity` records.
    *   Updates the locked `BalanceSnapshotEntity` by adding/subtracting the new amounts to keep the cache fresh.
    *   Commits transaction.

### 4.2 Handling Concurrency (Pessimistic Locking)
Under high load, multiple concurrent transfers might attempt to debit the same wallet. Optimistic locking (`@Version`) would cause high failure/retry rates. Thus, the Ledger uses pessimistic row-level locking on the `wallet_balance_snapshots` table strictly during the `PostJournalEntryUseCase` execution block.
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT b FROM BalanceSnapshotEntity b WHERE b.walletId = :walletId")
Optional<BalanceSnapshotEntity> findByWalletIdForUpdate(@Param("walletId") String walletId);
```

---

## 5. REST API & Internal DTOs
*Note: The Ledger is primarily an internal module consumed by other modules via Java method calls or events, but exposes a read API for dashboards.*

**GET** `/api/v1/ledger/wallets/{walletId}/balance`
```json
{
  "walletId": "wall_123",
  "ledgerBalance": "150000.00", 
  "availableBalance": "145000.00",
  "currency": "NGN"
}
```
*   **Ledger Balance:** All posted entries.
*   **Available Balance:** Settled credits minus all debits (including pending ones).

---

## 6. Database (MySQL 8)

**Security Note:** Application DB user is explicitly denied `UPDATE` and `DELETE` privileges on `ledger_entries`.

Flyway: `V1__ledger__001_create_ledger.sql`

```sql
CREATE TABLE ledger_entries (
    id VARCHAR(50) PRIMARY KEY,
    wallet_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    entry_type VARCHAR(10) NOT NULL, -- CREDIT, DEBIT
    reference VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    
    CONSTRAINT uq_idempotency UNIQUE (idempotency_key)
) PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p2027 VALUES LESS THAN (2028),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- Composite index optimized for the snapshot derivation and history querying
CREATE INDEX idx_wallet_created ON ledger_entries (wallet_id, created_at);

CREATE TABLE wallet_balance_snapshots (
    wallet_id VARCHAR(50) PRIMARY KEY,
    ledger_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    available_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL,
    last_updated_at DATETIME(6) NOT NULL
);
```
