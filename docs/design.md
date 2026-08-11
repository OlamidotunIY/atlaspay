# AtlasPay — Project Specification

## 1. What this is

AtlasPay is a **Java 21 / Spring Boot 3.x** payment infrastructure platform modeled after the
core product surface of Paystack / Flutterwave / Moniepoint: merchant onboarding, customer
management, sub-account split-payment configuration, real bank-backed virtual account issuance,
transfers, charges, subscriptions, escrow, settlement, and transaction reporting.

AtlasPay also supports optional commerce features: product inventory management, order
orchestration, and digital storefronts for merchants to sell directly through the platform.

This project is also an **intentional, comprehensive portfolio of senior Java engineering**. Every
module is designed so that the skills listed in §10 are demonstrably present in production-grade
code — not toy examples.

**Honesty constraint (do not violate in implementation):** AtlasPay is not a licensed bank or
BaaS provider. Real account issuance, NIP transfers, and payout settlement are delegated to
**Anchor** (BaaS sandbox). Identity verification is delegated to **Dojah** (KYC sandbox).
AtlasPay's engineering value is the domain model, ledger correctness, orchestration, and API
surface sitting on top of those providers — exactly the role Paystack/Flutterwave play on top
of Wema Bank / Titan Trust in production.

---

## 2. Tech Stack

| Layer | Choice | Notes |
|---|---|---|
| Language | Java 21 | Records, sealed classes, pattern matching, virtual threads |
| Framework | Spring Boot 3.x | Spring Core, MVC, Security, Data JPA, Actuator |
| Build | Gradle multi-module (Kotlin DSL) | Per-module `build.gradle.kts` |
| Database | **MySQL 8.x** | InnoDB, B-tree indexes, `utf8mb4` charset, strict mode |
| Schema migrations | Flyway | Namespaced per module: `V1__identity__*`, `V1__ledger__*` … |
| ORM | Spring Data JPA + Hibernate | Entity relationships, lazy/eager loading, N+1 guard |
| Messaging | Kafka (production) / in-memory (test/local) | Outbox pattern; swap without touching domain |
| Event serialization | Jackson | Custom `MoneySerializer` (BigDecimal, never double) |
| Cache | Redis (Spring Cache) | Rate limiting, idempotency key TTL |
| Auth | Spring Security + JWT + OAuth2 | Method security, role/permission model |
| Observability | SLF4J + Logback, Micrometer, OpenTelemetry | Prometheus/Grafana metrics, distributed tracing |
| Testing | JUnit 5, Mockito, Testcontainers | MySQL container for integration tests; WireMock for adapters |
| Containerisation | Docker / Docker Compose | Local dev; Kubernetes manifests for prod |
| CI/CD | GitHub Actions | Build, test, lint, Docker push |

---

## 3. External Sandbox Integrations

| Provider | Role | Consumed by module |
|---|---|---|
| **Anchor** (getanchor.co) | BaaS: merchant/sub-account deposit accounts, virtual NUBAN issuance, NIP transfers, payout, inbound webhook simulation | `atlaspay-accounts`, `atlaspay-transfers`, `atlaspay-settlement` |
| **Dojah** (dojah.io) | KYC: BVN/NIN verification, sandbox test data | `atlaspay-identity` |
| **Paystack** (optional, later) | Card collection channel, test mode | `atlaspay-charges` (additional adapter) |

Each provider is wrapped behind a **port interface** owned by the domain module.
Adapters live in `infrastructure/adapter/`. Nothing above the port interface knows which
provider is behind it.

---

## 4. Bounded Contexts — DDD Classification

### Full DDD (aggregate root, enforced invariants, explicit state machine)

| Module | Aggregate root | Key invariant |
|---|---|---|
| `atlaspay-identity` | Merchant, Customer, SubAccount | KycStatus transitions are one-way; BVN verification is idempotent; Customer email is unique per Merchant |
| `atlaspay-accounts` | VirtualAccount | Issuance is idempotent per owner; closure is irreversible |
| `atlaspay-ledger` | LedgerEntry | Append-only; balance always derived, never mutated directly |
| `atlaspay-transfers` | Transfer | State machine `PENDING→PROCESSING→SUCCESS/FAILED`; idempotency key enforced at DB level |
| `atlaspay-charges` | Charge (Refund is child entity, same boundary) | Refund total never exceeds charge's refundable balance |
| `atlaspay-subscriptions` | Subscription (Plan is catalog entity) | No double-billing per cycle; proration is correct |
| `atlaspay-escrow` | EscrowHold | Two-phase: `FUNDED→COMPLETED_PENDING_RELEASE→RELEASED`; native split support; clearance window enforced |
| `atlaspay-settlement` | SettlementBatch (SettlementLineItem children) | `effectiveAmount = totalProcessed − fees − deductions`; immutable once `SUCCESS` |
| `atlaspay-transaction-splits` | SplitConfiguration (SplitAllocation children) | Allocation shares must sum to 100 % of charge total |
| `atlaspay-products` *(optional)* | Product | Price and stock cannot be negative; product must belong to a Merchant |
| `atlaspay-orders` *(optional)* | Order (OrderLineItem children) | Total must equal sum of line items; status machine is strictly ordered |
| `atlaspay-storefronts` *(optional)* | Storefront | One active Storefront per Merchant; only published Products can be listed |

### Light / folded in — no standalone aggregate

| Concept | Lives inside | Why |
|---|---|---|
| Account-name resolution | `atlaspay-identity`, thin service | Stateless gateway call, no invariants |
| Transfer recipients (beneficiaries) | `atlaspay-transfers`, entity | CRUD with uniqueness constraint, no lifecycle |
| Plans | `atlaspay-subscriptions`, entity | Catalog/lookup data referenced by Subscription |
| SubAccount split configuration | `atlaspay-transaction-splits` | SubAccounts used here purely as split-payment recipients, not as identities |

### Query-only, no domain layer

| Module | What it is |
|---|---|
| `atlaspay-transactions-query` | CQRS read-model / projection unifying Charges, Transfers, Refunds, Ledger into one searchable history. No business rules of its own. |

---

## 5. Full Module & Folder Structure

```
atlaspay/
├── settings.gradle.kts
├── build.gradle.kts                    # root: Spring Boot plugin, Java 21 toolchain, shared deps
├── README.md
├── docs/
│   ├── design.md                       # this file
│   ├── adr/                            # Architecture Decision Records
│   └── domain-glossary.md             # ubiquitous language per bounded context
│
├── atlaspay-shared-kernel/             # zero Spring; pure Java 21 domain primitives
│   └── src/main/java/com/atlaspay/shared/
│       ├── money/                      # Money (BigDecimal + CurrencyCode), MoneySerializer
│       ├── event/                      # DomainEvent (record), DomainEventPublisher port
│       ├── idempotency/                # IdempotencyKey (record), helpers
│       ├── tracing/                    # CorrelationId propagation (MDC + thread-local)
│       ├── exception/                  # AtlasPayException hierarchy (sealed hierarchy)
│       └── util/                       # DateTimeUtils (java.time), PageResult (generic record)
│
├── atlaspay-identity/                  # KYC, merchant/customer/sub-account registration
│   └── src/main/java/com/atlaspay/identity/
│       ├── domain/
│       │   ├── model/                  # Merchant (aggregate), Customer (aggregate), SubAccount (aggregate), KycStatus (enum)
│       │   └── repository/             # MerchantRepository, CustomerRepository, SubAccountRepository (ports)
│       ├── application/
│       │   ├── usecase/                # RegisterMerchantUseCase, CreateCustomerUseCase,
│       │   │                           # RegisterSubAccountUseCase, VerifyIdentityUseCase
│       │   └── port/out/               # KycVerificationPort, AccountNameResolutionPort
│       ├── infrastructure/
│       │   ├── persistence/            # JPA entities + Flyway V1__identity__*
│       │   └── adapter/dojah/          # DojahClient, DojahKycAdapter
│       └── presentation/rest/          # MerchantController, CustomerController, SubAccountController
│
├── atlaspay-accounts/                  # Virtual account issuance (Anchor-backed)
│   └── src/main/java/com/atlaspay/accounts/
│       ├── domain/
│       │   ├── model/                  # VirtualAccount (aggregate), NUBAN (value object, record)
│       │   ├── event/                  # VirtualAccountIssued, VirtualAccountClosed (records)
│       │   └── repository/
│       ├── application/
│       │   ├── usecase/                # IssueVirtualAccountUseCase, CloseVirtualAccountUseCase
│       │   └── port/out/               # AccountIssuancePort
│       ├── infrastructure/
│       │   ├── persistence/            # Flyway V1__accounts__*
│       │   └── adapter/anchor/         # AnchorAccountAdapter
│       └── presentation/rest/
│
├── atlaspay-ledger/                    # Double-entry ledger — most rigorous module
│   └── src/main/java/com/atlaspay/ledger/
│       ├── domain/
│       │   ├── model/                  # LedgerEntry (append-only aggregate), EntryType (enum: DEBIT/CREDIT)
│       │   │                           # WalletBalanceSnapshot (read projection)
│       │   └── repository/
│       ├── application/
│       │   └── usecase/                # PostLedgerEntryUseCase (idempotency key enforced at DB level)
│       ├── infrastructure/
│       │   └── persistence/            # Flyway V1__ledger__*; composite index on (wallet_id, created_at)
│       └── presentation/rest/          # GET /ledger/balance, GET /ledger/entries
│
├── atlaspay-transfers/                 # Bank transfers (NIP via Anchor)
│   └── src/main/java/com/atlaspay/transfers/
│       ├── domain/
│       │   ├── model/                  # Transfer (aggregate), TransferRecipient (entity), TransferStatus (enum)
│       │   └── repository/
│       ├── application/
│       │   ├── usecase/                # InitiateTransferUseCase, RecordRecipientUseCase
│       │   ├── saga/                   # TransferProcessManager (Saga pattern)
│       │   └── retry/                  # EventRetryQueueService, CommandRetryRegistry
│       ├── infrastructure/
│       │   ├── persistence/            # Flyway V1__transfers__*
│       │   ├── adapter/anchor/         # AnchorTransferAdapter, webhook HMAC verification
│       │   └── messaging/              # Kafka producers/consumers (outbox pattern)
│       └── presentation/rest/          # POST /transfers, POST /webhooks/anchor/transfers
│
├── atlaspay-charges/                   # Payment collection (bank transfer + card)
│   └── src/main/java/com/atlaspay/charges/
│       ├── domain/
│       │   ├── model/                  # Charge (aggregate), Refund (child entity)
│       │   └── repository/
│       ├── application/
│       │   └── usecase/                # CreateChargeUseCase, RefundChargeUseCase
│       ├── infrastructure/
│       │   ├── persistence/
│       │   └── adapter/                # AnchorChargeAdapter; PaystackCardAdapter (later)
│       └── presentation/rest/
│
├── atlaspay-subscriptions/             # Recurring billing
│   └── src/main/java/com/atlaspay/subscriptions/
│       ├── domain/
│       │   ├── model/                  # Subscription (aggregate), Plan (catalog entity), BillingCycle (enum)
│       │   └── repository/
│       ├── application/
│       │   ├── usecase/                # CreateSubscriptionUseCase, CancelSubscriptionUseCase
│       │   └── scheduler/              # BillingCycleScheduler (Spring @Scheduled + virtual threads)
│       ├── infrastructure/persistence/
│       └── presentation/rest/
│
├── atlaspay-escrow/                    # Two-phase escrow holds
│   └── src/main/java/com/atlaspay/escrow/
│       ├── domain/model/               # EscrowHold: FUNDED → COMPLETED_PENDING_RELEASE → RELEASED (or DISPUTED)
│       ├── application/usecase/
│       ├── infrastructure/persistence/
│       └── presentation/rest/
│
├── atlaspay-settlement/                # Merchant settlement batching
│   └── src/main/java/com/atlaspay/settlement/
│       ├── domain/model/               # SettlementBatch (aggregate), SettlementLineItem (child)
│       ├── application/
│       │   ├── usecase/
│       │   └── scheduler/              # SettlementBatchScheduler
│       ├── infrastructure/persistence/
│       └── presentation/rest/          # GET /settlements, GET /settlements/{id}/transactions
│
├── atlaspay-transaction-splits/        # Revenue split configuration
│   └── src/main/java/com/atlaspay/splits/
│       ├── domain/model/               # SplitConfiguration (aggregate), SplitAllocation (child)
│       ├── application/usecase/
│       ├── infrastructure/persistence/
│       └── presentation/rest/
│
├── atlaspay-transactions-query/        # CQRS read-model (unified transaction history)
│   └── src/main/java/com/atlaspay/transactionsquery/
│       ├── projection/                 # listens to domain events via Kafka
│       ├── infrastructure/persistence/ # denormalized read table + full-text index
│       └── presentation/rest/          # GET /transactions (search, filter, pagination)
│
├── atlaspay-notifications/             # Async notification dispatch
│   └── src/main/java/com/atlaspay/notifications/
│       ├── domain/model/               # Notification, NotificationStatus (enum), Channel (enum: EMAIL/SMS/PUSH)
│       ├── application/
│       │   └── usecase/                # SendNotificationUseCase
│       ├── infrastructure/
│       │   ├── adapter/                # EmailAdapter, SmsAdapter (pluggable)
│       │   └── messaging/              # Kafka consumer — listens to domain events
│       └── presentation/rest/          # GET /notifications (history)
│
├── atlaspay-rate-limiter/              # Distributed rate limiting (Redis token-bucket)
│   └── src/main/java/com/atlaspay/ratelimiter/
│       ├── core/                       # RateLimiter (interface), TokenBucketRateLimiter
│       └── filter/                     # Spring Security filter integration
│
├── atlaspay-eventbus/                  # Internal event bus abstraction
│   └── src/main/java/com/atlaspay/eventbus/
│       ├── port/                       # EventPublisher, EventSubscriber (interfaces)
│       ├── inmemory/                   # InMemoryEventBus (local/test)
│       └── kafka/                      # KafkaEventBus (production) — outbox pattern impl
│
├── atlaspay-products/                  # [OPTIONAL] Merchant product/inventory management
│   └── src/main/java/com/atlaspay/products/
│       ├── domain/model/               # Product (aggregate): name, price (Money), stock, status (ACTIVE/ARCHIVED)
│       ├── application/usecase/        # CreateProductUseCase, UpdateStockUseCase, ArchiveProductUseCase
│       ├── infrastructure/persistence/ # Flyway V1__products__*
│       └── presentation/rest/          # ProductController
│
├── atlaspay-orders/                    # [OPTIONAL] Order orchestration for merchant products
│   └── src/main/java/com/atlaspay/orders/
│       ├── domain/
│       │   ├── model/                  # Order (aggregate), OrderLineItem (child entity), OrderStatus (enum)
│       │   └── repository/
│       ├── application/usecase/        # CreateOrderUseCase, FulfilOrderUseCase, CancelOrderUseCase
│       ├── infrastructure/persistence/ # Flyway V1__orders__*
│       └── presentation/rest/          # OrderController
│
├── atlaspay-storefronts/               # [OPTIONAL] Digital storefronts for merchants
│   └── src/main/java/com/atlaspay/storefronts/
│       ├── domain/model/               # Storefront (aggregate): slug, published Products listing
│       ├── application/usecase/        # CreateStorefrontUseCase, PublishProductUseCase
│       ├── infrastructure/persistence/ # Flyway V1__storefronts__*
│       └── presentation/rest/          # StorefrontController (public-facing + merchant management)
│
└── atlaspay-app/                       # Composition root (Spring Boot entry point)
    └── src/main/
        ├── java/com/atlaspay/app/      # @SpringBootApplication, SecurityConfig, GlobalExceptionHandler
        └── resources/
            ├── application.yml         # base config
            ├── application-local.yml   # local MySQL + in-memory bus
            ├── application-prod.yml    # prod datasource, Kafka, Redis
            └── db/migration/           # Flyway SQL (namespaced per module)
```

---

## 6. Database — MySQL 8.x

- **Engine**: InnoDB (ACID, row-level locking, MVCC)
- **Charset**: `utf8mb4`, collation `utf8mb4_unicode_ci`
- **SQL mode**: `STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO`
- **Monetary columns**: `DECIMAL(19,4)` — never `DOUBLE` or `FLOAT`
- **Timestamps**: `DATETIME(6)` (microsecond precision) + UTC application timezone
- **Migrations**: Flyway, namespaced per bounded context (`V{n}__{module}__{description}.sql`)
- **Testing**: Testcontainers MySQL image — real engine in CI, not H2

### Key MySQL design decisions

| Concern | Approach |
|---|---|
| Idempotency | `UNIQUE` constraint on `idempotency_key` per table; DB-enforced, not just app-layer |
| Ledger integrity | `ledger_entries` is insert-only; no `UPDATE`/`DELETE` via application; enforced by DB user grants |
| Optimistic locking | `@Version` on aggregates → `version` `INT` column; Hibernate `OptimisticLockException` on conflict |
| Pessimistic locking | `SELECT … FOR UPDATE` on `wallet_balance_snapshots` during high-contention balance reads |
| Indexes | B-tree (default); composite indexes on `(owner_id, created_at)`, `(status, created_at)`; `EXPLAIN` verified |
| Full-text search | `FULLTEXT` index on `transactions_query` read table for reference/description search |
| Partitioning | `ledger_entries` partitioned by `RANGE` on `YEAR(created_at)` for archival |
| Connection pooling | HikariCP (Spring Boot default) — max-pool-size tuned per service |
| Transactions | `@Transactional` at use-case boundary; isolation levels set explicitly where needed |

---

## 7. Cross-Cutting Patterns

### 7.1 Money & Financial Precision

- All monetary values use `Money` value object (wraps `BigDecimal` + `CurrencyCode` enum).
- **Never** `double` or `float` for money. Enforced by custom `ArchUnit` test rule.
- Rounding: `RoundingMode.HALF_EVEN` (banker's rounding) for fee splits.
- Currency conversion via `ExchangeRateProvider` port (pluggable; Fixer.io adapter in sandbox).
- Stored as `DECIMAL(19,4)` in MySQL.

### 7.2 Double-Entry Bookkeeping (Ledger module)

Every financial event produces **two** `LedgerEntry` records (debit + credit) within a single
transaction. Balance is always computed as `SUM(credit) - SUM(debit)` — never stored directly.
This ensures ledger is always in balance and audit-proof.

- `available_balance` = settled credits minus pending debits
- `ledger_balance` = all posted entries
- Journal entries are immutable records (Java `record` + insert-only DB table)

### 7.3 Idempotency

- Every mutating use case accepts an `IdempotencyKey`.
- Enforced at DB level via `UNIQUE` constraint — not just application-layer caching.
- Idempotent Kafka consumers: `UNIQUE(topic, partition, offset)` on processed message log.
- Retry semantics: at-least-once delivery + idempotent consumer = effectively-once processing.

### 7.4 Transactional Outbox + Kafka + Idempotent Consumer

```
[Use case] → [DB transaction: save aggregate + insert outbox row]
     ↓
[OutboxPoller] → [Kafka producer: publish event with message key]
     ↓
[Kafka consumer] → [check processed_messages table] → [idempotent handler]
```

- Outbox rows stored in `domain_events_outbox` MySQL table (per-module or shared).
- Kafka message key = aggregate ID → guarantees ordering within a partition.
- Dead-letter queue (DLQ) for poison messages after configurable retry backoff.

### 7.5 Transaction States & Payment Lifecycle

```
PENDING → PROCESSING → SUCCESS
                     ↘ FAILED → (REVERSAL / REFUND eligible)
```

- Authorization → Capture → Settlement → Reversal / Refund / Chargeback flows modelled
  explicitly as state machines in aggregate roots.
- `TransactionStatus` is a sealed class hierarchy (Java 21) or enum with transition guards.

### 7.6 Saga / Process Managers

Used wherever an operation spans modules asynchronously:
- `TransferProcessManager` — coordinates ledger debit → Anchor call → ledger credit/rollback
- `SubscriptionRenewalSaga` — billing cycle: charge → ledger → notification
- `SettlementBatchSaga` — aggregate settled charges → payout via Anchor → mark settled

### 7.7 Security

- **Authentication**: JWT (RS256) issued at login; `spring-security-oauth2-resource-server`
- **Authorization**: Role/permission model (`ROLE_MERCHANT`, `ROLE_SUB_ACCOUNT`, `ROLE_ADMIN`);
  method-level security with `@PreAuthorize`
- **OAuth2**: Integration with Anchor/Dojah using client-credentials flow for outbound calls
- **Secrets**: Never in `application.yml`; injected via environment variables / Kubernetes secrets
- **TLS**: Required in production; local dev uses self-signed cert
- **Password hashing**: BCrypt (Spring Security default)
- **Rate limiting**: `atlaspay-rate-limiter` module (Redis token-bucket), applied as a
  `OncePerRequestFilter` before authentication
- **Audit logging**: Every write operation logs `actor`, `action`, `resource_id`, `timestamp`
  to an immutable `audit_log` table
- **PII protection**: SubAccount/Merchant PII fields encrypted at rest (AES-256 via Jasypt); masked in logs
- **Compliance**: KYC/AML hooks in identity module; PCI DSS — card data never touches AtlasPay
  servers (tokenised at Paystack layer)

### 7.8 Correlation / Distributed Tracing

- `CorrelationId` generated at API gateway entry, propagated via HTTP headers (`X-Correlation-Id`)
  and Kafka message headers.
- Stored in `MDC` for structured logging; also injected into OpenTelemetry span attributes.

### 7.9 Retry & Resilience

- `EventRetryQueueService` — generic retry with exponential backoff + jitter.
- `CommandRetryRegistry` — per-command retry budget; reused across Transfers, Subscriptions,
  Settlement.
- Kafka consumer retry: Spring Kafka `SeekToCurrentErrorHandler` + DLQ topic.
- Resilience4j for circuit breakers on outbound Anchor / Dojah HTTP calls.

---

## 8. Concurrency & JVM Model

### Virtual Threads (Java 21)

- **All** blocking I/O paths (HTTP clients, JDBC, Kafka) run on virtual threads.
- Spring Boot 3.2+ auto-configures virtual thread executor for Tomcat and `@Async`.
- `BillingCycleScheduler` and `SettlementBatchScheduler` submit work via
  `Executors.newVirtualThreadPerTaskExecutor()`.

### Structured Concurrency & CompletableFuture

- Parallel external calls (e.g., BVN + NIN verification simultaneously) use
  `CompletableFuture.allOf(...)` with virtual thread executor.
- Fan-out settlement line item processing uses `StructuredTaskScope.ShutdownOnFailure`.

### Synchronization

- `ReentrantReadWriteLock` on in-memory rate-limiter state.
- `StampedLock` in `InMemoryEventBus` for high-read / low-write subscriber list.
- DB-level locking (`SELECT … FOR UPDATE`, `@Version`) preferred over application-level
  locks wherever possible.

---

## 9. Observability Stack

| Concern | Tool |
|---|---|
| Structured logging | SLF4J + Logback JSON layout (Logstash encoder) |
| Metrics | Micrometer → Prometheus; custom meters for payment success rate, ledger lag |
| Distributed tracing | OpenTelemetry SDK + OTLP exporter → Jaeger (local) / Grafana Tempo (prod) |
| Dashboards | Grafana (Prometheus + Tempo datasources) |
| Alerting | Alertmanager rules: error rate > 1 %, P99 > 2 s, DLQ depth > 0 |
| Health | Spring Actuator `/health`, `/metrics`, `/info`; readiness + liveness probes |
| JVM profiling | Java Flight Recorder (JFR) + Java Mission Control; heap/thread dumps on OOM |

---

## 10. Skills Coverage Map

This section maps every required skill to the module or pattern where it is **demonstrably implemented**.

### Java Language & Core

| Skill | Where |
|---|---|
| OOP & SOLID | Every domain module — single-responsibility use cases, open/closed aggregates, LSP-safe event hierarchy, ISP ports, DIP via dependency injection |
| Generics | `PageResult<T>`, `Result<T, E>`, `EventRetryQueueService<C>`, `Repository<T, ID>` |
| Collections | `atlaspay-ledger` (balance derivation via Stream over `List<LedgerEntry>`), settlement batch line items |
| Exceptions | Sealed `AtlasPayException` hierarchy; typed domain exceptions; Spring `@ControllerAdvice` handler |
| Records | `Money`, `IdempotencyKey`, `NUBAN`, `DomainEvent`, `LedgerEntry`, `PageResult`, `JournalEntry` |
| Enums | `KycStatus`, `TransferStatus`, `EntryType`, `CurrencyCode`, `Channel`, `BillingCycle`, `EscrowState` |
| Interfaces / abstract classes | Port interfaces (`KycVerificationPort`, `AccountIssuancePort`, `EventPublisher`); `BaseUseCase<I,O>` abstract class |
| Annotations | Custom annotations: `@IdempotencyKey`, `@AuditLog`, `@Monetary`; Hibernate/JPA annotations; Spring annotations |
| Streams & lambdas | Ledger balance aggregation, settlement fee computation, transaction query projections, notification fan-out |
| Optional | Repository return types; nullable external API fields; `Optional.map().orElseThrow()` chains |
| Date/Time API | `ZonedDateTime` for all timestamps; `LocalDate` for billing cycles; `Duration` for retry backoff; `java.time` throughout |
| BigDecimal | `Money` value object; `DECIMAL(19,4)` persistence; `RoundingMode.HALF_EVEN` for fees; never `double` |
| I/O | Outbox pattern file-based fallback; Kafka serialisation; `InputStream` for webhook body HMAC verification |
| Reflection | Custom annotation processor (`@AuditLog` interceptor via AOP + reflection); JPA entity inspector utility |
| JVM basics | JVM tuning docs + `jvm-tuning/` resource; heap size, stack size, GC flags in Dockerfile |
| Memory management | Off-heap Kafka buffers; virtual thread stack sizing; `WeakReference` in event subscriber registry |
| Garbage collection | G1GC (default Java 21); JFR GC event monitoring; explicit GC log configuration in startup flags |
| Multithreading | `BillingCycleScheduler`, `SettlementBatchScheduler`, parallel KYC verification |
| Concurrency | `ConcurrentHashMap` idempotency cache, `AtomicLong` sequence generators, `BlockingQueue` retry queue |
| Virtual threads | All Spring MVC request threads; JDBC pool threads; `@Async` beans; schedulers |
| Executors | `Executors.newVirtualThreadPerTaskExecutor()` for schedulers; bounded `ThreadPoolExecutor` for CPU tasks |
| CompletableFuture | Parallel BVN + NIN verification; async Anchor/Dojah HTTP calls; settlement fan-out |
| Synchronization & locks | `ReentrantReadWriteLock` (rate limiter); `StampedLock` (event bus); `synchronized` in idempotency key registry |

### Data Structures & Algorithms

| Skill | Where |
|---|---|
| Arrays | Batch settlement line item processing; byte arrays for HMAC computation |
| Strings | NUBAN validation, idempotency key generation, HMAC hex encoding |
| Linked lists | `LinkedList<RetryEntry>` in `EventRetryQueueService` |
| Stacks/queues | `ArrayDeque` as retry queue; `BlockingQueue` for DLQ simulation |
| Hash tables | `ConcurrentHashMap` idempotency store; `HashMap` in-memory event subscriber map |
| Trees | B-tree index design (documented in ADR); `TreeMap` for ordered transaction history |
| Heaps | `PriorityQueue<RetryEntry>` for priority-based retry scheduling |
| Graphs | Settlement dependency graph (which charges belong to which batch); cycle detection |
| Tries | Prefix-based merchant reference lookup (notifications routing) |
| Sorting | Settlement line item sorting by `created_at`; fee tier sorting |
| Searching | Binary search on sorted settlement batch entries; full-text search on read model |
| Recursion | Recursive fee split calculation; recursive retry-with-backoff |
| Backtracking | Split allocation solver (ensure allocations sum to 100 %) |
| Greedy algorithms | Greedy settlement batch packing (maximize batch size within Anchor payout window) |
| Dynamic programming | Optimal currency denomination for split payouts; retry backoff schedule computation |
| BFS/DFS | Event dependency resolution (determine order of saga steps); settlement graph traversal |
| Time/space complexity | All core algorithms documented with O(n) analysis in Javadoc |

### Spring Ecosystem

| Skill | Where |
|---|---|
| Spring Core / DI / IoC | Every module; constructor injection everywhere; no field injection |
| Beans / Application context | `@Configuration` classes per module; `@Profile`-gated beans |
| Configuration | `application.yml` hierarchy; `@ConfigurationProperties` records; `@Value` for simple scalars |
| Profiles | `local` (in-memory bus, WireMock), `integration-test` (Testcontainers), `prod` (Kafka, Redis) |
| Lifecycle | `SmartLifecycle` for graceful Kafka consumer shutdown; `@PostConstruct`/`@PreDestroy` |
| AOP | `@AuditLog` interceptor; `@Timed` Micrometer aspect; idempotency enforcement aspect |
| Spring Boot | Auto-configuration; actuator; banner; fat-jar packaging |
| REST APIs | OpenAPI 3 specs per module; `@RestController`, `@RequestBody`, `@PathVariable`; HATEOAS links |
| Validation | `@Valid` + Bean Validation 3.0; custom `@Monetary` constraint; problem+json error format |
| Exception handling | `@ControllerAdvice` + `ProblemDetail` (RFC 7807); typed domain exceptions → HTTP status mapping |
| Actuator | `/health`, `/metrics`, `/info`, `/env`; custom health indicators per module |
| Logging | Logback JSON; MDC (`correlationId`, `walletId`); request/response logging filter |
| Testing | `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`; Testcontainers MySQL; WireMock; MockMvc |
| Spring Data JPA | `JpaRepository`, `Specification`, `@Query`, `Pageable`, projections |
| Hibernate | Entity relationships (`@OneToMany`, `@ManyToOne`); lazy vs eager; `@EntityGraph`; N+1 detection |
| Transactions | `@Transactional`; `Propagation.REQUIRES_NEW` for ledger entries; `isolation = SERIALIZABLE` for balance reads |
| Entity relationships | FK constraints in MySQL; bidirectional mapping with `mappedBy`; cascade rules |
| Lazy/eager loading | Explicit `FetchType` on every `@OneToMany`; `@BatchSize` to prevent N+1 |
| N+1 queries | `@EntityGraph` on query methods; `JOIN FETCH` in JPQL; Hibernate statistics in tests |
| Query optimization | `EXPLAIN` plans in ADR; composite indexes; covering indexes for common queries |
| Specifications | `SpecificationBuilder` in `atlaspay-transactions-query` for dynamic filtering |
| Spring Security | JWT resource server; role/permission model; method security; security filter chain |
| Authentication | JWT validation; `UserDetailsService` implementation |
| Authorization | `@PreAuthorize("hasRole('MERCHANT')")`; permission evaluator |
| JWT | RS256 tokens; refresh token rotation; token blacklisting via Redis |
| OAuth2 | Client-credentials for Anchor/Dojah outbound; authorization-code for merchant dashboard |
| Roles/permissions | `Permission` enum; `GrantedAuthority` mapping; dynamic permission loading |
| Method security | `@EnableMethodSecurity`; `@PreAuthorize`, `@PostAuthorize`, `@Secured` |
| Security filters | `CorrelationIdFilter`, `RateLimitFilter`, `JwtAuthenticationFilter` (ordered chain) |

### Database (MySQL)

| Skill | Where |
|---|---|
| Indexes | B-tree indexes on all FK columns and query predicates; composite indexes documented in ADR |
| B-tree | All MySQL indexes are B-tree; documented with query plan analysis |
| Composite indexes | `(merchant_id, status, created_at)` on charges; `(wallet_id, created_at)` on ledger |
| Query plans | `EXPLAIN` output captured in integration tests via `io.hypersistence.utils` |
| Transactions | `@Transactional` with explicit propagation; nested transaction in outbox |
| ACID | Relied on via InnoDB; demonstrated in ledger double-entry and idempotency tests |
| Isolation levels | `READ_COMMITTED` default; `SERIALIZABLE` for balance snapshot reads |
| Locks | `SELECT … FOR UPDATE` for wallet balance; `@Version` for optimistic locking |
| Deadlocks | Deadlock detection in integration tests; consistent lock ordering to prevent |
| MVCC | InnoDB MVCC leveraged for non-blocking reads in high-concurrency ledger queries |
| Constraints | `NOT NULL`, `UNIQUE`, `CHECK` (MySQL 8+), `FOREIGN KEY` on all relationships |
| Foreign keys | All cross-table references via FK with `ON DELETE RESTRICT` |
| Optimistic/pessimistic locking | `@Version` (optimistic) for aggregates; `LockModeType.PESSIMISTIC_WRITE` for balance |
| Partitioning | `ledger_entries` RANGE partitioned by year |
| Replication | Architecture supports read replicas; Spring DataSource routing via `AbstractRoutingDataSource` |
| Connection pooling | HikariCP; pool size = `(core_count * 2) + 1`; leak detection enabled |

### Financial Domain

| Skill | Where |
|---|---|
| BigDecimal / precision | `Money` value object; `DECIMAL(19,4)`; `RoundingMode.HALF_EVEN` |
| Currency handling | `CurrencyCode` enum (NGN, USD, GBP); multi-currency `Money` |
| Rounding | Explicit rounding at every arithmetic boundary |
| Double-entry bookkeeping | `atlaspay-ledger`; every event = debit + credit journal entry |
| Available vs ledger balance | `LedgerService.availableBalance()` vs `LedgerService.ledgerBalance()` |
| Immutable transaction records | `LedgerEntry` is a Java `record`; insert-only DB table |
| Payment lifecycle | Authorization → Capture → Settlement → Refund / Chargeback state machine |
| Transaction states | `TransactionStatus` enum with guard transitions |
| Idempotency keys | DB `UNIQUE` constraint; Redis TTL cache for fast lookup |

### Messaging & Distributed Systems

| Skill | Where |
|---|---|
| Kafka | `atlaspay-eventbus` Kafka impl; Testcontainers Kafka in integration tests |
| RabbitMQ | Optional adapter (pluggable behind `EventPublisher` port) |
| At-least-once delivery | Kafka consumer with manual ack; idempotent handler |
| Exactly-once semantics | Outbox pattern + idempotent consumer achieves effectively-once |
| Ordering | Kafka partition key = aggregate ID |
| Consumer groups | Separate consumer group per module |
| Dead-letter queues | DLQ topic per domain event type |
| Outbox pattern | `domain_events_outbox` table polled by `OutboxPoller` |
| Saga pattern | `TransferProcessManager`, `SettlementBatchSaga` |
| Eventual consistency | Documented as trade-off in ADR; compensating transactions for rollback |

### Testing

| Skill | Where |
|---|---|
| JUnit 5 | All tests; `@ParameterizedTest` for boundary/edge-case coverage |
| Mockito | Unit tests; `@Mock`, `@InjectMocks`, `ArgumentCaptor` |
| Integration testing | `@SpringBootTest` + Testcontainers MySQL + Testcontainers Kafka |
| Spring Boot testing | `@WebMvcTest`, `@DataJpaTest`, `@RestClientTest` per slice |
| Testcontainers | MySQL 8, Kafka, Redis containers in `integration-test` profile |
| Contract testing | Spring Cloud Contract (producer side) for adapter interfaces |
| TDD | Use cases developed test-first; failing test → green → refactor |
| Database testing | `@Sql` scripts for fixture data; `@Transactional` test rollback |
| Kafka integration testing | Embedded Kafka + Testcontainers Kafka for end-to-end event flow |

### Observability

| Skill | Where |
|---|---|
| SLF4J + Logback | Structured JSON logs; MDC for correlationId, walletId |
| Metrics | Micrometer counters/timers; custom payment success/failure meters |
| Distributed tracing | OpenTelemetry; trace propagated across HTTP and Kafka |
| Prometheus / Grafana | Metrics scrape config; pre-built dashboards in `ops/grafana/` |
| Alerting | Alertmanager rules in `ops/alerting/` |

### Infrastructure & Deployment

| Skill | Where |
|---|---|
| JVM memory / GC | G1GC tuning flags; JFR recording on startup; heap/thread dump scripts |
| Container memory limits | `Dockerfile` with `-XX:MaxRAMPercentage=75` |
| Health checks | Spring Actuator; Kubernetes liveness/readiness probes |
| Graceful shutdown | `SmartLifecycle`; Kafka consumer drain on `SIGTERM` |
| Kubernetes | `k8s/` manifests: Deployment, Service, ConfigMap, HorizontalPodAutoscaler |
| Horizontal scaling | Stateless services; session affinity via JWT; Kafka consumer group rebalancing |
| AWS | ECS/EKS deployment guide; RDS MySQL; MSK (managed Kafka); SQS/SNS as optional bus |

### Security

| Skill | Where |
|---|---|
| OWASP Top 10 | ADR documenting mitigations for injection, broken auth, SSRF, etc. |
| Encryption at rest | AES-256 for PII fields (Jasypt); RDS encryption enabled |
| Encryption in transit | TLS 1.2+ required; HSTS header; mutual TLS for internal services |
| Audit logging | Immutable `audit_log` table; AOP-driven; tamper-evident |
| API rate limiting | Redis token-bucket; `atlaspay-rate-limiter` module |
| Secrets management | No secrets in code; env vars / Kubernetes secrets / AWS Secrets Manager |

### Networking

| Skill | Where |
|---|---|
| HTTP/HTTPS | Spring MVC REST; `RestClient` (Java 21) for outbound calls |
| TLS | Server-side TLS config; mutual TLS for Anchor webhook validation (HMAC) |
| REST | Full CRUD + event endpoints; OpenAPI 3 specs |
| gRPC | Internal service-to-service (optional; adapter behind port interface) |
| WebSockets | Real-time transaction status push (notifications module) |
| Load balancers | NGINX reverse proxy config in `ops/`; AWS ALB in prod |
| Connection pooling | HikariCP (DB); Apache HttpClient 5 pool (outbound HTTP) |
| Timeouts | Connection + read timeouts on all outbound clients; configurable per adapter |
| Retries | Resilience4j retry on outbound adapters; exponential backoff + jitter |

---

## 11. Suggested Build Order

### Core (Required)
1. `atlaspay-shared-kernel` — Money, records, exception hierarchy, idempotency utils
2. `atlaspay-identity` — Merchant, Customer & SubAccount registration + Dojah KYC adapter
3. `atlaspay-accounts` — Virtual account issuance + Anchor adapter
4. `atlaspay-ledger` — Double-entry ledger (most rigorous module; all others depend on it)
5. `atlaspay-eventbus` — In-memory first; Kafka impl later
6. `atlaspay-transfers` — NIP transfers + Saga + Anchor webhook receiver
7. `atlaspay-charges` — Payment collection + refunds
8. `atlaspay-subscriptions` — Recurring billing + scheduler
9. `atlaspay-escrow` — Two-phase escrow
10. `atlaspay-transaction-splits` — Revenue splits (SubAccount split-payment configuration)
11. `atlaspay-settlement` — Merchant settlement batching (depends on ledger + transfers)
12. `atlaspay-transactions-query` — CQRS read model (depends on events from all above)
13. `atlaspay-notifications` — Async notification dispatch
14. `atlaspay-rate-limiter` — Redis rate limiting (wired in `atlaspay-app`)
15. `atlaspay-app` — Composition root; security config; global exception handler

### Optional Commerce Extensions (build after core is stable)
16. `atlaspay-products` — Product/inventory management (depends on identity)
17. `atlaspay-orders` — Order orchestration (depends on products + charges + identity)
18. `atlaspay-storefronts` — Digital storefronts (depends on products + orders)

---

## 12. Non-Goals / Credibility Statements (for README)

- Not a licensed bank or PSSP; account issuance and NIP transfers are delegated to Anchor's sandbox.
- KYC results are sandbox/test data from Dojah, not connected to real BVN/NIN databases.
- Settlement payouts move through Anchor's sandbox, not real money.
- Card data never touches AtlasPay servers — tokenised entirely at the Paystack layer.
- `SubAccount` in AtlasPay maps to Paystack's **Subaccounts API** (split-payment recipients only), not to customer identities.
- `Customer` in AtlasPay maps to Paystack's **Customers API** — a distinct concept from SubAccount.
- The Products, Orders, and Storefronts modules map to Paystack's optional commerce APIs and are feature-flagged off by default in `application.yml`.