# ADR-007: Enforce Idempotency at the Database Level, Not Just Application Level

## Status
Accepted

## Date
2026-08-11

## Context
Payment operations must be idempotent: if a client retries a failed or timed-out
request, the server must not process it twice. Duplicate processing can cause:
- Double charges to a sub-account
- Double credits to a wallet
- Double payouts to a merchant

Common approaches to idempotency:
1. **Application-level cache** (Redis `SET NX`) — fast lookup, but the cache can expire or be
   evicted before the DB transaction commits, creating a race window
2. **Application-level check-then-insert** — not atomic: two concurrent requests can both pass
   the check before either inserts
3. **Database-level `UNIQUE` constraint** — atomic by definition; the DB guarantees no two rows
   with the same key ever coexist

## Decision
**Idempotency is enforced by a `UNIQUE` constraint on `idempotency_key` in every mutating
table.** Application-level checks are a performance optimisation only, never the primary guard.

### Pattern
Every use case that mutates state (initiates a transfer, creates a charge, posts a ledger
entry) accepts an `IdempotencyKey`. The key is stored as a unique column in the relevant table.

```sql
-- In transfers table
ALTER TABLE transfers
    ADD COLUMN idempotency_key VARCHAR(64) NOT NULL,
    ADD UNIQUE INDEX ux_transfers_idempotency_key (idempotency_key);

-- In ledger_entries table
ALTER TABLE ledger_entries
    ADD COLUMN idempotency_key VARCHAR(64) NOT NULL,
    ADD UNIQUE INDEX ux_ledger_idempotency_key (idempotency_key);
```

### Application behaviour on duplicate
```java
try {
    transferRepository.save(transfer); // contains idempotency_key
} catch (DataIntegrityViolationException e) {
    if (isDuplicateKeyViolation(e)) {
        // idempotent: return the existing record
        return transferRepository.findByIdempotencyKey(command.idempotencyKey());
    }
    throw e;
}
```

On a duplicate key, the use case returns the **existing** result as if the operation
succeeded — the client cannot distinguish a first call from a retry.

### Key generation
`IdempotencyKey` is supplied by the client in the `Idempotency-Key` HTTP header.
Format: UUID v4 (`550e8400-e29b-41d4-a716-446655440000`).
If not provided, the server generates one — but the client will not benefit from retry
safety without providing their own stable key.

### Redis cache (performance layer, not correctness layer)
A Redis `SET NX` with TTL is used as a fast-path pre-check to avoid hitting the DB on
obvious duplicates. But:
- If Redis is down, the DB constraint is the fallback
- If the cache says "not seen" but the DB constraint fires, the DB wins
- The Redis entry is written **after** the DB commit, not before

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Redis-only idempotency (`SET NX`) | Redis can evict entries under memory pressure. A cleared cache means the DB has no guard. A restarted Redis loses all idempotency history. Insufficient for financial operations. |
| Application-level `findByIdempotencyKey` before insert | Check-then-act race: two concurrent retries can both find no existing record, both insert, one succeeds, one corrupts. |
| No idempotency mechanism | Unacceptable in a payment system. Network retries and client bugs will cause duplicate operations without idempotency. |

## Consequences

### Positive
- Idempotency is guaranteed even if Redis is down, app cache is cold, or the app crashes mid-transaction
- DB `UNIQUE` constraint is atomic — no race condition possible
- `DataIntegrityViolationException` on duplicate is a clear, testable signal
- Works transparently with Kafka consumer retries: idempotent consumers check `processed_messages`
  table with the same `UNIQUE` constraint pattern

### Negative / Trade-offs
- Every mutating table requires an `idempotency_key` column + unique index
- Slightly increased index maintenance overhead on inserts
- Clients must supply stable `Idempotency-Key` headers on retries; documentation and SDK
  enforcement are important
- Key expiry: after 24 hours, idempotency keys for completed operations can be archived
  (a background cleanup job is needed — documented in ADR-009)

## References
- [Idempotency Keys — Stripe Engineering](https://stripe.com/blog/idempotency)
- [Implementing Idempotency — AWS Builders Library](https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/)
