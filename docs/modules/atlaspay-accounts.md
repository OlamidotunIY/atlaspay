# Module Design Document — `atlaspay-accounts`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview
The `atlaspay-accounts` module is responsible for the issuance and lifecycle management of real-world Virtual Bank Accounts (NUBANs). It acts as the gateway for inbound fiat, assigning unique banking details to Merchants and Customers via asynchronous integration with an external BaaS provider (simulated by `atlaspay-simulator`).

---

## 2. Folder Structure

```text
atlaspay-accounts/
└── src/main/java/com/atlaspay/accounts/
    ├── domain/
    │   ├── model/
    │   │   ├── VirtualAccount.java         # Aggregate Root
    │   │   ├── AccountStatus.java          # Enum
    │   │   ├── OwnerType.java              # Enum
    │   │   └── NUBAN.java                  # Value Object (Record)
    │   ├── event/
    │   │   ├── VirtualAccountIssuanceRequested.java  # Record
    │   │   ├── VirtualAccountActivated.java          # Record
    │   │   └── VirtualAccountClosed.java             # Record
    │   ├── exception/
    │   │   └── AccountsErrorCode.java      # Enum implementing ErrorCode
    │   └── repository/
    │       └── VirtualAccountDomainRepository.java # Outbound Port
    ├── application/
    │   ├── command/
    │   │   ├── IssueVirtualAccountCommand.java     # Record
    │   │   ├── ActivateVirtualAccountCommand.java  # Record
    │   │   ├── RequestClosureCommand.java          # Record
    │   │   └── ForceCloseAccountsCommand.java      # Record
    │   ├── query/
    │   │   └── GetVirtualAccountsQuery.java        # Record
    │   ├── dto/
    │   │   ├── VirtualAccountDto.java              # Record
    │   │   └── AccountIssuanceRequestDto.java      # Record
    │   ├── usecase/
    │   │   ├── IssueVirtualAccountUseCase.java     # Extends BaseUseCase
    │   │   ├── ActivateVirtualAccountUseCase.java  # Extends BaseUseCase
    │   │   └── ForceCloseAccountsUseCase.java      # Extends BaseUseCase
    │   ├── saga/
    │   │   └── AccountIssuanceSaga.java            # Listens to Domain Events
    │   └── port/out/
    │       ├── AccountIssuancePort.java            # HTTP Port
    │       └── VirtualAccountQueryService.java     # Read-model Port
    ├── infrastructure/
    │   ├── entity/
    │   │   └── VirtualAccountEntity.java           # JPA Entity
    │   ├── repository/
    │   │   └── JpaVirtualAccountRepository.java    # Spring Data Repository
    │   ├── adapter/
    │   │   ├── persistence/
    │   │   │   └── VirtualAccountPersistenceAdapter.java # Implements DomainRepository
    │   │   ├── simulator/
    │   │   │   └── SimulatorAccountAdapter.java    # Implements AccountIssuancePort
    │   │   └── query/
    │   │       └── VirtualAccountQueryAdapter.java # Implements QueryService
    │   └── messaging/
    │       ├── AccountEventPublisher.java          # Outbox Kafka publisher
    │       └── MerchantBannedEventListener.java    # Kafka Consumer
    └── presentation/rest/
        ├── VirtualAccountController.java           # Merchant/Customer APIs
        └── SimulatorWebhookController.java         # Public webhook receiver
```

---

## 3. Domain Layer

### 3.1 Aggregate Root: `VirtualAccount`
**Class:** `public final class VirtualAccount extends AggregateRoot<VirtualAccountId>`

**Fields:**
*   `private final String ownerId;` (Merchant or Customer ID)
*   `private final OwnerType ownerType;` (Enum: `MERCHANT`, `CUSTOMER`)
*   `private final String accountName;` (e.g., "AtlasPay - John Doe")
*   `private final String bankName;` (e.g., "TITAN_TRUST")
*   `private NUBAN nuban;` (Record: `record NUBAN(String value)`. Nullable initially.)
*   `private AccountStatus status;` (Enum: `PENDING_ISSUANCE`, `ACTIVE`, `CLOSURE_REQUESTED`, `CLOSED`)

**Methods & Invariants:**
*   `public static VirtualAccount requestIssuance(VirtualAccountId id, String ownerId, OwnerType type, String accountName, String bankName)`
    *   Creates account with status `PENDING_ISSUANCE`.
    *   `this.registerEvent(new VirtualAccountIssuanceRequested(id, ownerId, bankName));`
*   `public void activate(NUBAN nuban)`
    *   Validates: `if (this.status != PENDING_ISSUANCE) throw new BusinessRuleException(AccountsErrorCode.ALREADY_ACTIVE);`
    *   Sets status to `ACTIVE`, assigns `NUBAN`.
    *   `this.registerEvent(new VirtualAccountActivated(this.id, this.nuban));`
*   `public void forceClose()`
    *   Sets status to `CLOSED`.
    *   `this.registerEvent(new VirtualAccountClosed(this.id, this.ownerId));`

---

## 4. Application Layer

### 4.1 Commands & DTOs
*   `record IssueVirtualAccountCommand(@IdempotencyKey String idempotencyKey, String ownerId, OwnerType ownerType, String bankName) implements Command`
*   `record ActivateVirtualAccountCommand(String referenceId, String nuban) implements Command`
*   `record VirtualAccountDto(String id, String ownerId, String accountName, String nuban, String bankName, String status)`

### 4.2 Use Cases (Implementations of `BaseUseCase`)

**`IssueVirtualAccountUseCase`**
1. Receives `IssueVirtualAccountCommand`.
2. **Rule Enforcement:** Queries `VirtualAccountQueryService` to check limits.
    *   If `ownerType == MERCHANT`, ensure `< 2` accounts, and ensure no existing account with the same `bankName`.
    *   If `ownerType == CUSTOMER`, ensure `0` accounts.
3. Creates `VirtualAccount` via `requestIssuance(...)`.
4. Saves via `VirtualAccountDomainRepository.save(account)`. (This automatically inserts outbox domain events via `BaseUseCase`'s infrastructure).
5. Returns `VirtualAccountDto`.

**`ActivateVirtualAccountUseCase`**
1. Receives `ActivateVirtualAccountCommand` (triggered by Webhook).
2. Loads `VirtualAccount` by ID (referenceId).
3. Calls `account.activate(new NUBAN(command.nuban()))`.
4. Saves aggregate.

### 4.3 Sagas / Orchestration
**Merchant Account Issuance Saga**
*   Listens to `MerchantComplianceCompletedEvent` from the Identity module.
*   Automatically dispatches **two** `IssueVirtualAccountCommand`s for the merchant: one specifying `Wema` and the other `Zenith`.
*   Note: The constraint that limits the system to Nigerian merchants is strictly enforced at the Identity/Registration boundary. The Accounts module inherently trusts that any issuance request is valid.

**Customer Account Issuance**
*   Unlike merchants, customer account creation is **NOT** event-driven.
*   It is triggered via a direct REST endpoint (`POST /api/v1/dedicated_accounts`).
*   The system randomly selects either `Wema` or `Zenith` and dispatches a **single** `IssueVirtualAccountCommand` for the customer.

**`AccountIssuanceSaga` (The Webhook Listener)**
*   Listens to `VirtualAccountIssuanceRequested` (from Kafka/Outbox).
*   Executes asynchronously using Java 21 Virtual Threads.
*   Maps event to `AccountIssuanceRequestDto(accountName)`. (Does not validate bank name or country here).
*   Calls `accountIssuancePort.requestIssuance(...)` which makes the HTTP call to the Simulator.
*   **No Polling:** Saga terminates here. It waits for the webhook.

---

## 5. Infrastructure Layer

### 5.1 Persistence Adapter
*   **Entity:** `VirtualAccountEntity` maps to DB table `virtual_accounts`.
*   **Locking:** Uses `@Version private Integer version;` for Optimistic Locking to prevent concurrent modifications (e.g., closing an account while it's being activated).
*   **Idempotency:** Enforced via Database `UNIQUE` index on the `idempotency_key` column for issuance requests.

### 5.2 Simulator Adapter (`SimulatorAccountAdapter`)
*   Implements `AccountIssuancePort`.
*   Uses `org.springframework.web.client.RestClient` (Java 21).
*   Makes an outbound `POST` request to the Simulator's URL (configured via `@Value("${atlaspay.simulator.url}")`).
*   Handles timeouts using Resilience4j `CircuitBreaker`.

---

## 6. REST API Surface

### 6.1 Dedicated Accounts (Customers)
*   **POST** `/api/v1/dedicated_accounts`
    *   **Headers:** `Idempotency-Key: uuid`
    *   **Body:** `{"customerId": "cus_123", "bankName": "Wema"}`
    *   **Security:** `merchantId` resolved from JWT/API Key.
    *   **Response:** `202 Accepted`

*   **GET** `/api/v1/dedicated_accounts`
    *   **Security:** `merchantId` resolved from JWT/API Key.
    *   **Response:** `200 OK` (List of accounts)

*   **GET** `/api/v1/dedicated_accounts/{accountId}`
    *   **Response:** `200 OK` (Account details)

*   **DELETE** `/api/v1/dedicated_accounts/{accountId}`
    *   **Description:** Deactivates the virtual account.
    *   **Response:** `204 No Content`

### 6.2 Simulator Webhook Receiver
*   **POST** `/api/v1/accounts/webhooks/simulator`
*   **Security:** Public endpoint, validates `X-Simulator-Signature` header (HMAC-SHA512 of payload).
*   **Body:**
```json
{
  "event": "virtual_account.created",
  "data": {
    "reference": "acc_12345",
    "nuban": "0123456789"
  }
}
```
*   **Logic:** Verifies HMAC. Dispatches `ActivateVirtualAccountCommand`. Returns `200 OK`.

---

## 7. Database (MySQL 8)

**Flyway:** `V1__accounts__001_create_virtual_accounts.sql`

```sql
CREATE TABLE virtual_accounts (
    id VARCHAR(50) PRIMARY KEY,
    owner_id VARCHAR(50) NOT NULL,
    owner_type VARCHAR(20) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    bank_name VARCHAR(50) NOT NULL,
    nuban VARCHAR(10) NULL,
    status VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    
    CONSTRAINT uq_nuban UNIQUE (nuban),
    CONSTRAINT uq_merchant_bank UNIQUE (owner_id, bank_name)
);

CREATE INDEX idx_owner ON virtual_accounts (owner_id, owner_type);
```
