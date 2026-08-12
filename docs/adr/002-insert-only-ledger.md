# ADR-002: Ledger Entries Are Insert-Only (No UPDATE / DELETE)

## Status
Accepted

## Date
2026-08-11

## Context
The `atlaspay-ledger` module is the single source of truth for all wallet balances
in the system. Every financial event — incoming transfer, outgoing payment, fee
deduction, refund — must be permanently and verifiably recorded.

If `ledger_entries` rows can be updated or deleted:
- A software bug could silently mutate historical balances
- A compromised service account could alter transaction history
- Auditors and regulators cannot rely on the record as a truthful ledger

Financial ledgers in traditional double-entry accounting are **append-only by definition**.
A ledger is a book of record; you do not erase from it — you write a correcting entry.

## Decision
`LedgerEntry` rows in the `ledger_entries` table are **never updated or deleted**.

Enforcement layers (defence in depth):
1. **Application layer**: `LedgerEntry` is a Java `record` (immutable by construction).
   `PostLedgerEntryUseCase` only calls `repository.save()` — never `update*` or `delete*`.
2. **ORM layer**: No `@DynamicUpdate`, no `merge()` calls on `LedgerEntry` entities.
3. **Database layer**: The application DB user is granted `INSERT` and `SELECT` only on
   `ledger_entries`. `UPDATE` and `DELETE` are revoked and tested in CI.
4. **Balance derivation**: Wallet balance is always computed as:
   `SELECT SUM(CASE WHEN type='CREDIT' THEN amount ELSE -amount END) FROM ledger_entries WHERE wallet_id = ?`
   Balance is never stored as a mutable column.

Corrections are made by posting a **reversing journal entry** (a new row that offsets
the erroneous entry), not by modifying the original row.

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Mutable balance column on a `wallets` table | Fast reads, but balance can silently drift if any code path runs a partial update (e.g., after a crash between debit and credit). Impossible to audit. |
| Soft-delete via `deleted_at` column | Still allows mutation of the record. Does not satisfy audit requirements. |
| Event sourcing (event store, no SQL table) | Provides the same guarantee but adds significant infrastructure complexity (event store, projection rebuilding). Append-only SQL table is simpler and gives the same audit guarantee at this scale. |

## Consequences

### Positive
- Ledger is tamper-evident and auditable by definition
- Balance correctness is provable: re-run the aggregation query on any point-in-time snapshot
- Regulatory compliance is simpler: regulators can query the raw table directly
- No race condition between "update balance" and "record the entry" — there is only one operation
- Compatible with `WalletBalanceSnapshot` cache: snapshot is a read-optimization, not source of truth

### Negative / Trade-offs
- Balance reads are aggregation queries — O(n) over all entries for a wallet
- Mitigated by:
  - Composite index on `(wallet_id, entry_type, created_at)`
  - `WalletBalanceSnapshot` table: a periodically checkpointed balance (used as a starting point
    for aggregation, not the final answer)
  - `RANGE` partitioning of `ledger_entries` by year — queries typically scan only recent partition
- The `ledger_entries` table grows indefinitely; partitioning + archival strategy required
  (documented in ADR-009)

## References
- [Double-Entry Bookkeeping — Wikipedia](https://en.wikipedia.org/wiki/Double-entry_bookkeeping)
- [Append-Only Ledger Pattern — Martin Fowler](https://martinfowler.com/eaaDev/EventSourcing.html)
