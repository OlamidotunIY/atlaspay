# ADR-010: Saga Pattern for Distributed / Multi-Step Transactions

## Status
Accepted

## Date
2026-08-11

## Context
Several AtlasPay operations span multiple modules and cannot be wrapped in a single
database transaction:

| Operation | Steps | Modules involved |
|---|---|---|
| Outbound transfer | Debit ledger → Call Anchor → Credit/rollback ledger | `transfers`, `ledger` |
| Subscription renewal | Charge sub-account → Post ledger entries → Notify sub-account | `subscriptions`, `charges`, `ledger`, `notifications` |
| Settlement batch | Aggregate charges → Compute fees → Payout via Anchor → Mark settled | `settlement`, `ledger`, `transfers` |

A distributed transaction (XA / 2PC) across multiple databases would provide atomicity
but: Kafka does not support XA; 2PC has known availability problems; Spring + Hibernate
XA support is complex and rarely used correctly.

## Decision
Multi-step operations that span module boundaries use the **Choreography-based Saga pattern**,
coordinated by a **Process Manager (Saga Orchestrator)** per operation type.

### Choreography vs Orchestration choice

We use **orchestration** (a dedicated process manager class) rather than pure choreography
(each module reacts to events and emits the next):
- Orchestration makes the full flow visible in one place (easier to debug, test, change)
- Choreography can produce implicit flows that are hard to reason about when steps fail

### Process manager structure
```java
@Component
public class TransferProcessManager {

    // Step 1: on TransferInitiated event → post debit to ledger
    @EventHandler
    public void on(TransferInitiated event) {
        ledgerPort.postDebit(event.walletId(), event.amount(), event.idempotencyKey());
        // Publishes LedgerDebitPosted event on success
    }

    // Step 2: on LedgerDebitPosted → call Anchor
    @EventHandler
    public void on(LedgerDebitPosted event) {
        var ref = anchorPort.initiateTransfer(event.transferId());
        // Publishes AnchorTransferSubmitted or AnchorTransferFailed
    }

    // Step 3a: on AnchorTransferCompleted → post credit to recipient ledger, mark SUCCESS
    @EventHandler
    public void on(AnchorTransferCompleted event) { ... }

    // Step 3b: on AnchorTransferFailed → post compensating credit (reversal), mark FAILED
    @EventHandler
    public void on(AnchorTransferFailed event) {
        ledgerPort.postCompensatingCredit(event.walletId(), event.amount(), event.compensationKey());
        transferRepository.markFailed(event.transferId());
    }
}
```

### Compensating transactions (rollback)
When a saga step fails, we do not roll back previous steps (they are committed in other
modules). Instead, we execute a **compensating transaction** — a new operation that logically
undoes the effect:

| Failed step | Compensation |
|---|---|
| Anchor call failed | Post `CREDIT` journal entry to reverse the `DEBIT` |
| Notification failed | Retry notification only (ledger/transfer already committed — non-critical) |
| Settlement payout failed | Mark `SettlementBatch` as `FAILED`; alert operations; retry scheduled |

### Saga state persistence
Each saga instance stores its state in the `saga_state` table. If the process manager
crashes mid-flow, it resumes from the last known state on restart.

```sql
CREATE TABLE saga_state (
    saga_id      VARCHAR(36)  PRIMARY KEY,
    saga_type    VARCHAR(100) NOT NULL,
    current_step VARCHAR(100) NOT NULL,
    payload      JSON         NOT NULL,
    status       VARCHAR(20)  NOT NULL,  -- RUNNING | COMPLETED | COMPENSATING | FAILED
    updated_at   DATETIME(6)  NOT NULL
);
```

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Distributed transactions (XA / 2PC) | Kafka does not support XA. Known availability problems (coordinator SPOF). Unacceptable complexity. |
| Pure choreography (event-driven, no coordinator) | Implicit flows are difficult to trace, test, and change. Fine for simple two-step flows; rejected for complex multi-step operations. |
| Ignore failures (best-effort) | Unacceptable in a payment system. Money lost or double-credited without compensation is a real financial harm. |

## Consequences

### Positive
- Explicit, visible flow for complex operations
- Crash-safe: saga state persisted; resumes after restart
- Compensating transactions handle partial failures correctly
- Each step is independently testable and retryable

### Negative / Trade-offs
- Eventual consistency: the system is in an intermediate state while the saga runs
  → unavoidable in distributed systems; UX handles this (show PENDING status)
- More complex than a single `@Transactional` method — appropriate only for genuinely
  distributed operations; single-module operations still use simple transactions
- `saga_state` table must be monitored; stuck sagas need alerting and manual intervention tooling

## References
- [Saga Pattern — microservices.io](https://microservices.io/patterns/data/saga.html)
- [Process Manager Pattern — Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/patterns/messaging/ProcessManager.html)
- [Choreography vs Orchestration — Bernd Rücker](https://blog.bernd-ruecker.com/the-microservice-workflow-automation-cheat-sheet-fc0a80dc25aa)
