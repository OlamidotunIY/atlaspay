# Module Design Document — `atlaspay-eventbus`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview

### 1.1 Purpose
This module provides centralized, highly-reliable messaging infrastructure for the entire AtlasPay system. It acts as the bridge between internal Domain Events (Spring ApplicationContext) and the external distributed message broker (Kafka), guaranteeing that no domain event is ever lost via the **Transactional Outbox Pattern**.

### 1.2 Scope — What This Module Does
*   Listens to `EnvelopedDomainEvent` published within the JVM.
*   Saves events to a relational database table (`outbox_messages`) synchronously with the domain transaction.
*   Polls the outbox table asynchronously and publishes payloads to Kafka.
*   Updates message status upon successful Kafka acknowledgment.

### 1.3 Out of Scope — What This Module Does NOT Do
*   It does **not** define domain events (this is the responsibility of `atlaspay-shared-kernel` and specific domain modules).
*   It does **not** consume Kafka events (other modules like `atlaspay-notifications` or `atlaspay-ledger` act as consumers).
*   It executes absolutely zero business rules.

### 1.4 Dependencies

| Dependency | Type | Reason |
|---|---|---|
| `atlaspay-shared-kernel` | Internal module | Needs `DomainEventPublisher` and `EnvelopedDomainEvent` definitions. |
| `spring-kafka` | External library | For `KafkaTemplate` publishing. |

---

## 2. Domain Model (Outbox)

While this is an infrastructure module, the Outbox pattern forms a mini-domain for reliable messaging.

### 2.1 Aggregate Roots

#### `OutboxMessage`

**Identity:** `UUID`

**Fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `id` | `UUID` | No | Unique message ID (often same as event correlation ID) |
| `topic` | `String` | No | The target Kafka topic (e.g., `merchant-events`) |
| `payload` | `String` | No | Serialized JSON representation of the domain event |
| `status` | `OutboxStatus` | No | `PENDING`, `SENT`, or `FAILED` |
| `createdAt` | `ZonedDateTime` | No | Timestamp of creation |
| `processedAt` | `ZonedDateTime` | Yes | Timestamp when Kafka acknowledged receipt |

---

## 3. Domain Events
*N/A - This module translates events, it does not generate its own.*

---

## 4. Repository Ports

### `OutboxMessageRepository`

| Method Signature | Returns | Notes |
|---|---|---|
| `save(OutboxMessage message)` | `void` | Persists a new outbox entry |
| `findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status)` | `List<OutboxMessage>` | Used by the scheduler to pull a batch of pending events |

---

## 5. Application Layer (CQRS)

### `SaveOutboxMessageUseCase`
**Type:** Command (Internal)
**Action:** Takes an `EnvelopedDomainEvent`, serializes it to JSON, and saves an `OutboxMessage` with `PENDING` status.

### `ProcessOutboxMessagesUseCase`
**Type:** Command (Internal/Cron)
**Action:** Fetches batches of `PENDING` messages, sends them via the `MessageBrokerPort`, and updates their status to `SENT`.

---

## 6. Outbound Ports (External Dependencies)

### `MessageBrokerPort`
| Method | Parameters | Returns | Description |
|---|---|---|---|
| `send(...)` | `String topic, String key, String payload` | `void` | Publishes the JSON payload to the broker |

**Adapter implementation:** `KafkaMessageBrokerAdapter` in `infrastructure/messaging/` using Spring's `KafkaTemplate`.

---

## 7. REST API Surface
*None. This module operates entirely in the background via internal events and cron schedules.*

---

## 8. Database

### Tables

#### `outbox_messages`

| Column | MySQL Type | Nullable | Notes |
|---|---|---|---|
| `id` | `CHAR(36)` | No | UUID primary key |
| `topic` | `VARCHAR(255)` | No | Target Kafka topic |
| `payload` | `JSON` / `TEXT` | No | Serialized event |
| `status` | `VARCHAR(20)` | No | `PENDING`, `SENT` |
| `created_at` | `DATETIME(6)` | No | UTC |
| `processed_at` | `DATETIME(6)` | Yes | UTC |
| `version` | `INT` | No | Optimistic locking (`@Version`) |

**Indexes:**
| Index Name | Columns | Type | Reason |
|---|---|---|---|
| `idx_outbox_status_created` | `status`, `created_at` | B-tree | For extremely fast cron polling |

**Flyway migration file:** `V1__eventbus__create_outbox_messages.sql`
