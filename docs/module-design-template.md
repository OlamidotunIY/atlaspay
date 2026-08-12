# Module Design Document — `atlaspay-[module-name]`

> **Status:** `DRAFT` | `IN REVIEW` | `APPROVED` | `SUPERSEDED`
> **Author:** [Your Name]
> **Created:** YYYY-MM-DD
> **Last Updated:** YYYY-MM-DD
> **Reviewers:** [Names or GitHub handles]

---

## 1. Overview

### 1.1 Purpose
<!-- One paragraph. What is this module's single responsibility? What problem does it solve? -->

### 1.2 Scope — What This Module Does
<!--
- Bullet list of what this module owns and is responsible for.
-->

### 1.3 Out of Scope — What This Module Does NOT Do
<!--
- Bullet list of things that might seem related but are explicitly NOT this module's concern.
- This prevents scope creep and clarifies boundaries with other modules.
-->

### 1.4 Dependencies
<!--
Which other AtlasPay modules does this module depend on?
Which external providers does it integrate with?
-->

| Dependency | Type | Reason |
|---|---|---|
| `atlaspay-shared-kernel` | Internal module | Money, DomainEvent, AggregateRoot, exceptions |
| `[other-module]` | Internal module | [reason] |
| `[Provider Name]` | External (HTTP) | [what it does] |

---

## 2. Domain Model

### 2.1 Aggregate Roots

For each aggregate root, define the following:

---

#### `[AggregateName]`

**Identity:** `[AggregateNameId]` (from `atlaspay-shared-kernel` or defined here)

**Fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `id` | `[AggregateNameId]` | No | Aggregate identity |
| `[field]` | `[Type]` | Yes/No | [Description and any constraints] |

**State Machine (if applicable):**
```
STATE_A → STATE_B → STATE_C
                 ↘ STATE_D (terminal)
```

**Invariants:**
- [ ] [Statement of a rule the aggregate always enforces, e.g., "Amount cannot be negative"]
- [ ] [Another invariant]

**Domain Methods (behaviours):**

| Method | Parameters | Returns | Throws | Description |
|---|---|---|---|---|
| `methodName(...)` | `ParamType param` | `void` | `SomeException` | What it does and what invariant it enforces |

**Domain Events Raised:**

| Event | Raised When | Key Fields |
|---|---|---|
| `[EventName]` | [condition] | `[field1]`, `[field2]` |

---

### 2.2 Value Objects

#### `[ValueObjectName]`

| Field | Type | Validation Rule |
|---|---|---|
| `[field]` | `[Type]` | [e.g., "Cannot be null or blank; must match regex X"] |

> Implemented as a Java `record` with a compact constructor that throws `ValidationException` on violation.

---

### 2.3 Enums

| Enum | Values | Notes |
|---|---|---|
| `[EnumName]` | `VALUE_A`, `VALUE_B` | [Purpose and usage] |

---

## 3. Domain Events

All events implement `DomainEvent` from `atlaspay-shared-kernel`.
All events are Java `record`s — immutable by construction.

| Event Record | Raised By | Fields | Published To |
|---|---|---|---|
| `[EventName]` | `[AggregateName].[method]()` | `eventId`, `aggregateId`, `occurredAt`, `correlationId`, `[other fields]` | `[Kafka topic / in-memory bus]` |

---

## 4. Repository Ports

Repository interfaces live in `domain/repository/`. Implementations (Adapters) live in `infrastructure/adapter/persistence/`. Spring Data JPA interfaces live in `infrastructure/repository/` and JPA Entities in `infrastructure/entity/`.

### `[AggregateNameRepository]`

Extends `Repository<AggregateName, AggregateNameId>` from shared-kernel.

| Method Signature | Returns | Notes |
|---|---|---|
| `save(AggregateName entity)` | `AggregateName` | Inherited |
| `findById(AggregateNameId id)` | `Optional<AggregateName>` | Inherited |
| `existsById(AggregateNameId id)` | `boolean` | Inherited |
| `findBy[Field]([FieldType] value)` | `Optional<AggregateName>` | [specific query reason] |

---

## 5. Application Layer (CQRS)

The application layer follows CQRS (Command Query Responsibility Segregation) pattern.
- **Commands**: Live in `application/command/`
- **Queries**: Live in `application/query/`
- **Use Cases (Handlers)**: Live in `application/usecase/`
- **Data Transfer Objects (DTOs)**: Live in `application/dto/`

Each use case extends `BaseUseCase<Input, Output>` from `atlaspay-shared-kernel`. For use cases that do not return a result (mutating commands), they should extend `BaseUseCase<Input, Void>` and return `null`.

---

### `[UseCaseName]`

**Type:** Command (mutating) / Query (read-only)

**Input Command / Query:**

```java
public record [UseCaseName]Command(
    String idempotencyKey,
    // ... fields
) {}
```

| Field | Type | Validation |
|---|---|---|
| `[field]` | `[Type]` | [e.g., "Required; non-blank"] |

**Output:**

```java
// What the use case returns (record, Void, etc.)
```

**Happy Path:**
1. [Step 1 — e.g., Validate input]
2. [Step 2 — e.g., Check for duplicate]
3. [Step 3 — e.g., Create aggregate]
4. [Step 4 — e.g., Persist via repository]
5. [Step 5 — e.g., Publish domain events]

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| [e.g., Email already exists] | `ConflictException` | `[MODULE_CONFLICT_CODE]` |
| [e.g., Entity not found] | `NotFoundException` | `[MODULE_NOT_FOUND_CODE]` |

---

## 6. Outbound Ports (External Dependencies)

Ports live in `application/port/`. Adapters live in `infrastructure/adapter/`.

### `[PortName]`

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `[methodName](...)` | `[Params]` | `[ReturnType]` | [What the domain needs from outside] |

**Adapter implementation:** `[AdapterClassName]` in `infrastructure/adapter/[provider]/`

---

## 7. REST API Surface

Base path: `/api/v1/[module-path]`

Authentication: `Bearer JWT` (required unless marked public)

### Endpoints

---

#### `POST /[path]`

**Description:** [What this endpoint does]

**Request Body:**
```json
{
  "field": "value"
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `[field]` | `string` | Yes | [validation rule] |

**Response `201 Created`:**
```json
{
  "id": "uuid",
  "field": "value"
}
```

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `400` | `[CODE]` | [when] |
| `409` | `[CODE]` | [when] |

---

## 8. Database

Module prefix for Flyway migrations: `V{n}__[module]__*.sql`

### Tables

#### `[table_name]`

| Column | MySQL Type | Nullable | Notes |
|---|---|---|---|
| `id` | `CHAR(36)` | No | UUID primary key |
| `[column]` | `[TYPE]` | Yes/No | [description] |
| `created_at` | `DATETIME(6)` | No | UTC |
| `updated_at` | `DATETIME(6)` | No | UTC |
| `version` | `INT` | No | Optimistic locking (`@Version`) |

**Indexes:**

| Index Name | Columns | Type | Reason |
|---|---|---|---|
| `PRIMARY` | `id` | B-tree | — |
| `uq_[table]_[field]` | `[field]` | UNIQUE | [idempotency / uniqueness rule] |
| `idx_[table]_[field]` | `[field]` | B-tree | [query pattern] |

**Flyway migration file:** `V1__[module]__create_[table_name].sql`

---

## 9. Error Codes

All error codes are defined in `[Module]ErrorCode` enum implementing `ErrorCode` from `atlaspay-shared-kernel`.

| Error Code | Exception Type | HTTP Status | When Thrown |
|---|---|---|---|
| `[MODULE_SOMETHING_WRONG]` | `NotFoundException` | 404 | [description] |
| `[MODULE_SOMETHING_EXISTS]` | `ConflictException` | 409 | [description] |
| `[MODULE_RULE_VIOLATED]` | `BusinessRuleException` | 422 | [description] |

---

## 10. Open Questions / Decisions Pending

> Use this section during design review. Remove resolved items before marking the doc `APPROVED`.

- [ ] [Question or decision that needs team input]
- [ ] [Another open question]
