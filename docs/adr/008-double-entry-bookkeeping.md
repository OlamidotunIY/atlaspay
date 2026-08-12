# ADR-008: Double-Entry Bookkeeping for All Financial Transactions

## Status
Accepted

## Date
2026-08-11

## Context
Single-entry bookkeeping records only one side of a financial movement:
"Customer A was debited ₦5,000."

This is simple but has a critical weakness: if there is a bug in the code that posts
the debit but fails before posting the corresponding credit, money disappears.
There is no structural way to detect this — the ledger cannot tell you it is wrong.

Double-entry bookkeeping, invented in 15th-century Italy and still the global standard
for financial accounting, solves this: every financial event produces **two entries
(a debit and a credit) that must always sum to zero**.

```
Fundamental invariant: SUM(all debits) == SUM(all credits)
```

If this invariant is ever violated, the books are "out of balance" — and you know
immediately that something went wrong.

## Decision
**All financial events in AtlasPay produce exactly two `LedgerEntry` records — a debit
and a matching credit — posted in the same atomic database transaction.**

### Account structure

| Account | Type | Increases with | Decreases with |
|---|---|---|---|
| SubAccount wallet | Asset | CREDIT | DEBIT |
| Merchant wallet | Asset | CREDIT | DEBIT |
| AtlasPay fee income | Revenue | CREDIT | DEBIT |
| Liability (escrow hold) | Liability | DEBIT | CREDIT |
| Settlement payable | Liability | DEBIT | CREDIT |

### Example: SubAccount charges ₦10,000, AtlasPay fee 1.5%

```
Journal Entry: CHARGE-001
  DEBIT  sub_account_wallet   ₦10,000.0000    (sub-account pays)
  CREDIT merchant_wallet       ₦9,850.0000    (merchant receives net)
  CREDIT atlaspay_fee_income     ₦150.0000    (fee earned)
  ────────────────────────────────────────────
  SUM of DEBITS:              ₦10,000.0000
  SUM of CREDITS:             ₦10,000.0000  ✓ balanced
```

### Example: Outbound transfer ₦5,000

```
Journal Entry: TRANSFER-002
  DEBIT  merchant_wallet       ₦5,000.0000    (merchant sends)
  CREDIT settlement_payable    ₦5,000.0000    (AtlasPay owes to bank)
  ────────────────────────────────────────────
  SUM:   ₦5,000.0000 == ₦5,000.0000  ✓ balanced
```

### Balance derivation (per wallet)
```sql
SELECT
    SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE 0 END) -
    SUM(CASE WHEN entry_type = 'DEBIT'  THEN amount ELSE 0 END) AS balance
FROM ledger_entries
WHERE wallet_id = ?;
```

### Invariant check (runs in CI daily + on-demand)
```sql
-- Must always return 0.0000
SELECT SUM(CASE WHEN entry_type='DEBIT' THEN amount ELSE -amount END) AS net
FROM ledger_entries;
```

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Single-entry ledger (one row per transaction) | Cannot detect data corruption or partial writes. No structural balance invariant. Insufficient for regulated financial reporting. |
| Storing balance directly on wallet row | Fast reads, but balance can drift from reality on partial failures. No audit trail. Mutating a balance is not a financial record — it's a cached number. |

## Consequences

### Positive
- Ledger is provably correct: `SUM(debits) == SUM(credits)` at all times
- Any data corruption or code bug that causes a partial write is immediately detectable
- Provides a direct path to financial reporting, P&L statements, and regulatory audits
- Aligns AtlasPay's internal accounting with how real banks and payment processors work
- Fee reconciliation is trivial: query `atlaspay_fee_income` account entries

### Negative / Trade-offs
- Every financial event requires writing at least 2 rows instead of 1 → 2x write volume
  → acceptable; ledger writes are much rarer than ledger reads
- More complex domain model: developers must understand debit/credit semantics
  → mitigated by this ADR and the domain glossary
- `PostLedgerEntryUseCase` must always be called with a balanced `JournalEntry`
  → enforced by the `JournalEntry` value object which validates balance on construction:
  ```java
  public record JournalEntry(List<LedgerEntryLine> lines) {
      public JournalEntry {
          var net = lines.stream()
              .map(l -> l.type() == DEBIT ? l.amount().negate() : l.amount())
              .reduce(Money.ZERO, Money::add);
          if (net.isNotZero()) throw new UnbalancedJournalEntryException(net);
      }
  }
  ```

## References
- [Double-Entry Bookkeeping — Wikipedia](https://en.wikipedia.org/wiki/Double-entry_bookkeeping)
- [Accounting for Developers — Monzo Engineering](https://monzo.com/blog/2016/05/11/double-entry-accounting)
- [Ledger Pattern — Martin Fowler](https://martinfowler.com/apsupp/accounting.pdf)
