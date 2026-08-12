# ADR-003: Transactional Outbox Pattern for Reliable Event Publishing

## Status
Accepted

## Date
2026-08-11

## Context
AtlasPay modules communicate asynchronously via domain events (e.g., `TransferCompleted`,
`ChargeSucceeded`, `LedgerEntryPosted`). These events are published to Kafka and consumed
by downstream modules.

The naive approach — publish to Kafka inside the same use case that saves to MySQL — has
a critical flaw: **the two operations are not atomic**.

```
// BROKEN: Two separate I/O operations, no atomic guarantee
repository.save(transfer);           // MySQL commit
kafkaTemplate.send("transfers", e);  // Kafka publish — what if this fails?
```

Failure scenarios:
1. MySQL commits → Kafka publish fails → downstream modules never learn about the transfer
2. Kafka publishes → MySQL rollback → event published for a transaction that was rolled back
3. Application crashes between the two → split-brain state

In a payment system, either failure causes real financial harm: a failed notification leads
to a missed settlement, a phantom event leads to a double credit.

## Decision
All domain event publishing uses the **Transactional Outbox Pattern**:

1. Within the **same MySQL transaction** that saves the aggregate, insert a row into
   `domain_events_outbox` (same DB, same transaction = atomic).
2. A background `OutboxPoller` reads unpublished rows from `domain_events_outbox` and
   publishes them to Kafka.
3. After successful Kafka acknowledgement, the outbox row is marked `published = true`.
4. The Kafka consumer uses an **idempotent consumer** pattern (checks `processed_messages`
   table) to handle at-least-once delivery safely.

```
┌─────────────────────────────────────────────┐
│  MySQL Transaction                          │
│  ┌──────────────────┐  ┌─────────────────┐  │
│  │ aggregate table  │  │ outbox table    │  │
│  │ (e.g. transfers) │  │ (unpublished=1) │  │
│  └──────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────┘
                    ↓ (async)
             OutboxPoller
                    ↓
               Kafka Topic
                    ↓
        Idempotent Kafka Consumer
                    ↓
         processed_messages table
```

### Outbox table schema
```sql
CREATE TABLE domain_events_outbox (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_id  VARCHAR(36)  NOT NULL,
    event_type    VARCHAR(255) NOT NULL,
    payload       JSON         NOT NULL,
    published     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at    DATETIME(6)  NOT NULL,
    published_at  DATETIME(6),
    INDEX idx_outbox_unpublished (published, created_at)
);
```

## Alternatives Considered

| Option | Reason rejected |
|---|---|
| Direct Kafka publish inside `@Transactional` | Not atomic with DB commit. Producer can fail independently. Message may be published for rolled-back transactions. |
| Two-phase commit (XA transactions) | Theoretically correct but operationally nightmarish. Kafka does not support XA. Performance overhead unacceptable. |
| In-memory `ApplicationEventPublisher` (Spring) | Only works within a single JVM process. Lost on crash. Unsuitable for durable event propagation across services/restarts. |
| Change Data Capture (CDC) via Debezium | Valid alternative. Rejected for now: adds Debezium infrastructure complexity. Could replace the `OutboxPoller` in a future ADR without changing domain code (same outbox table). |

## Consequences

### Positive
- Exactly-once event delivery achievable: at-least-once (Kafka) + idempotent consumer = effectively-once
- No event is lost even if Kafka is down (events accumulate in outbox; poller retries)
- No phantom events for rolled-back transactions (outbox row is inside the same DB transaction)
- Domain code is decoupled from Kafka — uses `DomainEventPublisher` port; outbox is infrastructure

### Negative / Trade-offs
- Slight latency: event delivery is asynchronous (outbox poll interval, default 500ms)
  → acceptable for eventual consistency; not suitable for synchronous response requirements
- `OutboxPoller` must be a singleton per DB (or use pessimistic `SELECT … FOR UPDATE SKIP LOCKED`
  to safely run multiple pollers in a cluster)
- `processed_messages` table grows; requires periodic cleanup (TTL-based purge job)
- `OutboxPoller` is an additional component to monitor and alert on (DLQ depth metric)

## References
- [Transactional Outbox Pattern — microservices.io](https://microservices.io/patterns/data/transactional-outbox.html)
- [Reliable Microservices Data Exchange With the Outbox Pattern — Debezium](https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/)
