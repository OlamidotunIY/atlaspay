# Module Design Document — `atlaspay-simulator`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview
The `atlaspay-simulator` module provides a self-hosted Banking-as-a-Service (BaaS) replacement. It behaves exactly like an external core-banking/NIP rail: holding its own independent ledger, processing inbound triggers, failing deterministically based on test data, and communicating via webhooks with exponential backoff.

AtlasPay interacts with it strictly via HTTP, mimicking real-world boundaries.

---

## 2. Folder Structure

```text
atlaspay-simulator/
└── src/main/java/com/atlaspay/simulator/
    ├── domain/
    │   ├── model/
    │   │   ├── SimulatorAccount.java            # Entity
    │   │   ├── SimulatorLedgerEntry.java        # Immutable Record
    │   │   └── WebhookDelivery.java             # Entity for retry tracking
    │   └── repository/
    │       ├── SimulatorAccountRepository.java
    │       └── WebhookDeliveryRepository.java
    ├── application/
    │   ├── command/
    │   │   ├── ProcessInboundTransferCommand.java
    │   │   └── ProcessOutboundPayoutCommand.java
    │   ├── usecase/
    │   │   ├── ProcessInboundTransferUseCase.java
    │   │   └── ProcessOutboundPayoutUseCase.java
    │   └── scheduler/
    │       ├── WebhookRetryScheduler.java       # @Scheduled job for exponential backoff
    │       └── ReconciliationJobScheduler.java  # Cross-ledger diffing
    ├── infrastructure/
    │   ├── adapter/
    │   │   └── OutboundWebhookClient.java       # RestClient to hit AtlasPay
    │   └── persistence/
    │       ├── SimulatorAccountJpaEntity.java
    │       └── SimulatorLedgerJpaEntity.java
    └── presentation/rest/
        ├── SimulatorInboundTriggerController.java
        └── SimulatorPayoutController.java
```

---

## 3. Core Mechanisms & Algorithms

### 3.1 Inbound NIP Transfers (External → AtlasPay)
**Goal:** Mimic the 2-stage lifecycle of real NIP transfers.
1.  **Generate Correlation ID:** When `ProcessInboundTransferUseCase` is invoked, generate a NIP Session ID (`100YYYYMMDDHHMMSSNNNNNN`).
2.  **Write to Mirror Ledger:** Insert a credit into `SimulatorLedgerEntry`.
3.  **Fire Webhooks (Virtual Threads):** 
    *   Using Java 21 Virtual Threads (`Thread.startVirtualThread(...)`), pause for an artificial delay (`Thread.sleep(Duration.ofSeconds(new Random().nextInt(4) + 1))`).
    *   Fire `transfer.completed` webhook. This forces AtlasPay to correctly handle asynchronous `PENDING` states rather than assuming instant settlement.

### 3.2 Outbound Payouts & The Timeout Saga (AtlasPay → Simulator)
**Goal:** Deterministic failure injection based on destination account numbers (never rely on magic amounts).

**Implementation in `ProcessOutboundPayoutUseCase`:**
Uses a switch expression with Java Pattern Matching to determine outcome:
```java
return switch(command.destinationAccountNumber()) {
    case "9000000001" -> processSuccess(command);
    case "9000000002" -> processTimeoutSimulation(command); // Delays 2m, returns FAILED
    case "9000000003" -> processImmediateFailure(command, "INVALID_ACCOUNT");
    default -> processSuccess(command);
};
```
**The Timeout Saga (AtlasPay Side):** When the simulator times out (`9000000002`), AtlasPay must **not** assume failure. AtlasPay's `TransferProcessManager` Saga will execute `GET /simulator/transfers/{sessionId}` before issuing any compensating transaction to roll back the ledger debit.

### 3.3 Webhook Delivery Reliability (Exponential Backoff)
**Algorithm:**
The `WebhookRetryScheduler` runs periodically to find failed webhooks.
1.  **Formula:** Next attempt time = `createdAt + (baseDelay * 2 ^ attemptCount)`.
2.  **Delays:** 5s, 30s, 5min, 1hr.
3.  **Threshold:** Max 5 attempts. If exceeded, `status = DEAD_LETTERED`.

---

## 4. Double-Entry Settlement (Mirror Ledger)
The simulator maintains a mathematically strict `simulator_ledger` table.

**The Reconciliation Job (`ReconciliationJobScheduler`):**
A CRON job that proves AtlasPay's core ledger has no bugs.
*   **Logic:**
    1. Query AtlasPay internal ledger: `SUM(credit) - SUM(debit)` for virtual account `X`.
    2. Query Simulator mirror ledger: `SUM(credit) - SUM(debit)` for virtual account `X`.
    3. Assert `Diff == 0`. 
*   A non-zero diff proves a critical failure in idempotency, a dropped webhook, or a race condition in the core app.

---

## 5. REST API & DTOs

### 5.1 Account Generation Endpoint (Called by AtlasPay)
**POST** `/simulator/mock/accounts/issue`
**DTO:**
```java
record AccountGenerationRequestDto(
    @NotBlank String referenceId,
    @NotBlank String accountName,
    @NotBlank String callbackUrl
) {}
```
**Response:** `202 Accepted` (Simulator will process asynchronously and POST to `callbackUrl`).
*   **Logic:**
    1. Determine Bank Code (e.g., `035` for Wema, `057` for Zenith) based on internal logic.
    2. Generate unique 9-digit `account_serial` from DB sequence.
    3. Calculate 1-digit Check Digit using CBN modulo-10 algorithm.
    4. Concat to form 10-digit NUBAN.
    5. Fire webhook to `callbackUrl`.

### 5.2 Inbound Transfer Trigger
**POST** `/simulator/mock/inbound-transfer`
**DTO:**
```java
record InboundTransferRequestDto(
    @NotBlank String destinationAccountNumber,
    @Positive long amount, // kobo
    @NotBlank String senderName,
    @NotBlank String senderAccountNumber,
    @NotBlank String senderBankCode
) {}
```
**Response:** `202 Accepted`

### 5.2 Outbound Payout Endpoint (Called by AtlasPay)
**POST** `/simulator/mock/payouts`
**DTO:**
```java
record PayoutRequestDto(
    @NotBlank String reference,
    @Positive long amount,
    @NotBlank String destinationAccount,
    @NotBlank String destinationBank
) {}
```

---

## 6. Database (MySQL 8)

Flyway: `V1__simulator__001_initial_schema.sql`

```sql
CREATE TABLE simulator_accounts (
    id VARCHAR(50) PRIMARY KEY,
    reference VARCHAR(100) NOT NULL,
    bank_name VARCHAR(50) NOT NULL,
    bank_code VARCHAR(3) NOT NULL,
    account_serial BIGINT NOT NULL UNIQUE, -- The 9-digit unique serial
    nuban VARCHAR(10) NULL,
    account_name VARCHAR(100) NOT NULL,
    callback_url VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL
);

CREATE TABLE simulator_ledger (
    id VARCHAR(50) PRIMARY KEY,
    nuban VARCHAR(10) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    entry_type VARCHAR(10) NOT NULL, -- CREDIT or DEBIT
    reference VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL
);

CREATE TABLE simulator_webhooks (
    id VARCHAR(50) PRIMARY KEY,
    payload JSON NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, DELIVERED, FAILED, DEAD_LETTERED
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL
);
```
