# AtlasPay

A production-grade **Java 21 / Spring Boot 3.x** payment infrastructure platform, modelled after the core product surface of Paystack, Flutterwave, and Moniepoint.

AtlasPay handles merchant onboarding, customer management, virtual account issuance, bank transfers, payment collection, recurring subscriptions, escrow, revenue splits, and settlement — all built on strict **Hexagonal Architecture**, **Domain-Driven Design**, and **CQRS** principles.

> **Portfolio context:** This project is an intentional, comprehensive demonstration of senior Java engineering. Every module is designed so that real-world payment engineering skills are demonstrably present in production-grade code — not toy examples. Real account issuance, NIP transfers, and identity verification are delegated to sandbox providers (Anchor BaaS and Dojah), exactly as Paystack/Flutterwave delegate to their banking partners.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Modules](#modules)
- [External Integrations](#external-integrations)
- [Key Engineering Patterns](#key-engineering-patterns)
- [Security Model](#security-model)
- [Getting Started](#getting-started)
- [Documentation](#documentation)
- [Architecture Decision Records](#architecture-decision-records)

---

## Tech Stack

| Layer | Choice | Notes |
|---|---|---|
| Language | **Java 21** | Records, sealed classes, pattern matching, virtual threads |
| Framework | **Spring Boot 3.5.x** | MVC, Security, Data JPA, Actuator |
| Build | **Gradle** (multi-module) | Per-module `build.gradle`; Java 21 toolchain |
| Database | **MySQL 8.x** | InnoDB, `DECIMAL(19,4)` for money, `DATETIME(6)` UTC timestamps |
| Migrations | **Flyway** | Namespaced per module: `V1__identity__*`, `V1__ledger__*` … |
| ORM | **Spring Data JPA + Hibernate** | Optimistic locking (`@Version`), N+1 guarded |
| Messaging | **Kafka** (prod) / in-memory (local/test) | Transactional outbox pattern |
| Cache | **Redis** | Rate limiting (token-bucket), idempotency key TTL |
| Auth | **Spring Security + JWT + API Keys** | Dual-credential model; method-level `@PreAuthorize` |
| Observability | **SLF4J / Logback, Micrometer, OpenTelemetry** | Prometheus/Grafana metrics; distributed tracing |
| Testing | **JUnit 5, Mockito, Testcontainers** | Real MySQL container in CI; WireMock for external adapters |
| Containerisation | **Docker / Docker Compose** | Local dev stack; Kubernetes manifests for production |
| CI/CD | **GitHub Actions** | Build, test, lint, Docker push |

---

## Architecture

AtlasPay follows **Hexagonal Architecture (Ports and Adapters)** combined with **DDD** and **CQRS**. Every module is self-contained and enforces this layering:

```
domain/          ← Aggregates, Value Objects, Domain Events, Repository Interfaces
application/     ← Use Cases (BaseUseCase<I,O>), Commands, Queries, DTOs, Outbound Ports
infrastructure/  ← JPA Repositories, REST/Kafka/External Adapters, Flyway migrations
presentation/    ← REST Controllers
```

**Key rules enforced across all modules:**
- Aggregates have **no public setters** — all state changes happen via explicit business methods.
- Domain events are immutable `record` types published via `BaseUseCase.publishEvents()` — no boilerplate per use case.
- Repository interfaces live in `domain/`; implementations live in `infrastructure/`.
- Commands (`*Command`) mutate state. Queries (`*Query`) read state. They never mix.
- `merchantId` is **never in the URL** for authenticated endpoints — always resolved from the security context.

See [`docs/design.md`](docs/design.md) for the complete specification.

---

## Modules

### Core Modules

| Module | Aggregate Root(s) | Status | Docs |
|---|---|---|---|
| `atlaspay-shared-kernel` | — | ✅ In progress | — |
| `atlaspay-identity` | `Merchant`, `Customer`, `SubAccount`, `ApiKey` | ✅ In progress | [identity.md](docs/modules/identity.md) |
| `atlaspay-accounts` | `VirtualAccount` | 🔲 Planned | — |
| `atlaspay-ledger` | `LedgerEntry` | 🔲 Planned | — |
| `atlaspay-transfers` | `Transfer`, `TransferRecipient` | 🔲 Planned | — |
| `atlaspay-charges` | `Charge` (+ `Refund` child entity) | 🔲 Planned | — |
| `atlaspay-subscriptions` | `Subscription`, `Plan` | 🔲 Planned | — |
| `atlaspay-escrow` | `EscrowHold` | 🔲 Planned | — |
| `atlaspay-settlement` | `SettlementBatch` | 🔲 Planned | — |
| `atlaspay-transaction-splits` | `SplitConfiguration` | 🔲 Planned | — |
| `atlaspay-transactions-query` | *(CQRS read model — no aggregate)* | 🔲 Planned | — |
| `atlaspay-notifications` | `Notification` | 🔲 Planned | — |
| `atlaspay-rate-limiter` | *(Cross-cutting filter — no aggregate)* | 🔲 Planned | — |
| `atlaspay-eventbus` | *(Infrastructure — no aggregate)* | 🔲 Planned | — |
| `atlaspay-app` | *(Composition root)* | ✅ In progress | — |

### Optional Commerce Modules

| Module | Aggregate Root(s) | Status |
|---|---|---|
| `atlaspay-products` | `Product` | 🔲 Planned |
| `atlaspay-orders` | `Order` | 🔲 Planned |
| `atlaspay-storefronts` | `Storefront` | 🔲 Planned |

---

## External Integrations

All third-party providers are wrapped behind **port interfaces** owned by the domain module. Adapters live in `infrastructure/adapter/`. Nothing above the port layer knows which provider is behind it.

| Provider | Role | Consumed By |
|---|---|---|
| **[Anchor](https://getanchor.co)** | BaaS: virtual NUBAN issuance, NIP transfers, payout, inbound webhooks | `atlaspay-accounts`, `atlaspay-transfers`, `atlaspay-settlement` |
| **[Dojah](https://dojah.io)** | BVN/NIN identity verification (sandbox) | `atlaspay-identity` |
| **[Paystack](https://paystack.com)** *(later)* | Card collection channel (test mode) | `atlaspay-charges` |

---

## Key Engineering Patterns

### Double-Entry Bookkeeping
Every financial event produces two `LedgerEntry` records (debit + credit) in a single transaction. Balance is always derived as `SUM(credit) − SUM(debit)` — never stored directly. The ledger is append-only and audit-proof.

### Transactional Outbox
```
Use Case → [DB transaction: save aggregate + insert outbox row]
         → OutboxPoller → Kafka producer
         → Kafka consumer → idempotent handler
```
Guarantees at-least-once delivery without dual writes.

### Idempotency
Every mutating use case accepts an `IdempotencyKey`. Enforced at the **database level** via `UNIQUE` constraints — not just application-layer caching.

### Saga / Process Managers
Used wherever an operation spans modules asynchronously:
- `TransferProcessManager` — ledger debit → Anchor call → ledger credit/rollback
- `SubscriptionRenewalSaga` — charge → ledger → notification
- `SettlementBatchSaga` — aggregate settled charges → payout → mark settled

### Virtual Threads (Java 21)
All blocking I/O (JDBC, HTTP calls) runs on virtual threads. No reactive libraries. Simple, readable, blocking-style code with the throughput profile of async I/O.

---

## Security Model

AtlasPay uses a **dual-credential model** — both credential types are resolved by the same filter chain, and `merchantId` is always derived from the credential, never from the URL.

| Credential | Format | Who Uses It | How `merchantId` is Resolved |
|---|---|---|---|
| **Secret API Key** | `Bearer sk_live_xxx` / `Bearer sk_test_xxx` | Server-to-server API calls | HMAC-SHA256(key, serverSecret) → lookup `api_keys.key_hash` |
| **JWT** | `Bearer eyJ...` | Dashboard (browser) | `sub` claim in JWT payload |

**Filter chain:** `RateLimitFilter → ApiKeyAuthFilter → JwtAuthFilter → Controller`

**API Keys:**
- `PUBLIC` keys (`pk_`) — safe for frontend embedding; stored in plaintext.
- `SECRET` keys (`sk_`) — server-side only; stored as HMAC-SHA256 hash; raw value shown once only.
- `TEST` vs `LIVE` environments. Live keys are only issued after compliance is `APPROVED`.

---

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- MySQL 8.x (or use the provided Compose file)

### Run Locally

```bash
# Clone the repository
git clone https://github.com/OlamidotunIY/atlaspay.git
cd atlaspay

# Start dependencies (MySQL, Redis, Kafka)
docker compose up -d

# Build all modules
./gradlew build

# Run the application
./gradlew :atlaspay-app:bootRun --args='--spring.profiles.active=local'
```

### Environment Variables

The following secrets must be provided via environment variables — never in `application.yml`:

| Variable | Description |
|---|---|
| `ATLASPAY_DB_URL` | JDBC connection string for MySQL |
| `ATLASPAY_DB_USERNAME` | Database username |
| `ATLASPAY_DB_PASSWORD` | Database password |
| `ATLASPAY_JWT_SECRET` | HS256 secret for signing JWTs |
| `ATLASPAY_APIKEY_HMAC_SECRET` | Server secret for HMAC-SHA256 API key hashing |
| `ANCHOR_API_KEY` | Anchor BaaS API key (sandbox) |
| `DOJAH_APP_ID` | Dojah application ID |
| `DOJAH_PRIVATE_KEY` | Dojah private API key |

---

## Documentation

| Document | Description |
|---|---|
| [`docs/design.md`](docs/design.md) | Full project specification: tech stack, bounded contexts, module structure, cross-cutting patterns |
| [`docs/domain-glossary.md`](docs/domain-glossary.md) | Ubiquitous language — precise definitions for every term used across all bounded contexts |
| [`docs/modules/identity.md`](docs/modules/identity.md) | Identity module deep-dive: aggregates, use cases, API endpoints, DB schema |
| [`docs/module-design-template.md`](docs/module-design-template.md) | Template for documenting new modules consistently |

---

## Architecture Decision Records

Major architectural choices are documented as ADRs in [`docs/adr/`](docs/adr/).

| # | Decision | Status |
|---|---|---|
| [001](docs/adr/001-mysql-over-postgresql.md) | MySQL over PostgreSQL | Accepted |
| [002](docs/adr/002-insert-only-ledger.md) | Insert-only ledger (no UPDATE/DELETE) | Accepted |
| [003](docs/adr/003-outbox-pattern.md) | Transactional outbox for event publishing | Accepted |
| [004](docs/adr/004-bigdecimal-for-money.md) | `BigDecimal` for all monetary values | Accepted |
| [005](docs/adr/005-virtual-threads.md) | Java 21 virtual threads over reactive | Accepted |
| [006](docs/adr/006-hexagonal-architecture.md) | Hexagonal architecture + DDD + CQRS | Accepted |
| [007](docs/adr/007-idempotency-at-db-level.md) | DB-level idempotency via `UNIQUE` constraints | Accepted |
| [008](docs/adr/008-double-entry-bookkeeping.md) | Double-entry bookkeeping for the ledger | Accepted |
| [009](docs/adr/009-jwt-authentication.md) | JWT + API Key dual-credential auth | Accepted |
| [010](docs/adr/010-saga-pattern.md) | Saga / process managers for cross-module flows | Accepted |
