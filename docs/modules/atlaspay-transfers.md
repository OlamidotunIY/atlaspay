# Module Design Document — `atlaspay-transfers`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview
The `atlaspay-transfers` module handles outbound payouts (NIP Transfers) from AtlasPay to external bank accounts. It interacts with the `atlaspay-simulator` (BaaS mock) to move money out of the platform. Because external transfers can timeout or fail asynchronously, this module relies heavily on Saga orchestration (`TransferProcessManager`) and compensating transactions to maintain ledger integrity.

---

## 2. Folder Structure

```text
atlaspay-transfers/
└── src/main/java/com/atlaspay/transfers/
    ├── domain/
    │   ├── model/
    │   │   ├── Transfer.java                    # Aggregate Root
    │   │   ├── TransferRecipient.java           # Entity
    │   │   ├── TransferStatus.java              # Enum (PENDING, PROCESSING, SUCCESS, FAILED, REVERSED)
    │   │   └── FailureReason.java               # Value Object
    │   ├── event/
    │   │   ├── TransferInitiated.java
    │   │   └── TransferCompleted.java
    │   └── repository/
    │       └── TransferDomainRepository.java
    ├── application/
    │   ├── command/
    │   │   └── InitiateTransferCommand.java
    │   ├── query/
    │   │   └── GetTransferStatusQuery.java
    │   ├── dto/
    │   │   └── TransferDto.java
    │   ├── usecase/
    │   │   ├── InitiateTransferUseCase.java
    │   │   └── ProcessTransferWebhookUseCase.java
    │   ├── saga/
    │   │   └── TransferProcessManager.java      # Saga orchestrating Ledger -> Simulator -> Rollbacks
    │   ├── retry/
    │   │   ├── EventRetryQueueService.java      # Handles webhook retries
    │   │   └── CommandRetryRegistry.java
    │   └── port/out/
    │       ├── CoreBankingTransferPort.java     # Hits Simulator
    │       └── InternalLedgerPort.java          # Cross-module call to atlaspay-ledger
    ├── infrastructure/
    │   ├── persistence/
    │   │   └── TransferEntity.java              # JPA Entity
    │   ├── adapter/
    │   │   ├── simulator/
    │   │   │   └── SimulatorTransferAdapter.java # Implements CoreBankingTransferPort
    │   │   └── ledger/
    │   │       └── LedgerGrpcAdapter.java       # Implements InternalLedgerPort
    │   └── messaging/
    │       └── TransferEventPublisher.java      # Outbox Kafka publisher
    └── presentation/rest/
        ├── TransferController.java              # Merchant APIs
        └── TransferWebhookController.java       # Receives Simulator callbacks
```

---

## 3. Domain Layer

### 3.1 Aggregate Root: `Transfer`
**Class:** `public final class Transfer extends AggregateRoot<TransferId>`

**Fields:**
*   `private final String merchantId;`
*   `private final Money amount;`
*   `private final Money fee;`
*   `private final TransferRecipient recipient;`
*   `private TransferStatus status;`
*   `private String providerReference;` (The Simulator's session ID)

**Methods & Invariants:**
*   `public void markProcessing(String providerReference)`
    *   `if (status != PENDING) throw new IllegalStateException();`
*   `public void markSuccess()`
    *   `if (status != PROCESSING) throw new IllegalStateException();`
*   `public void markFailed(FailureReason reason)`
    *   Triggers compensating event `TransferFailed` (picked up by Saga to refund Ledger).

---

## 4. Application Layer & Algorithms

### 4.1 The Transfer Saga (`TransferProcessManager`)
Transfers are distributed transactions. The Saga orchestrates this using compensating transactions.
1. **Initiation (`InitiateTransferUseCase`)**:
   * Saves `Transfer` as `PENDING`.
   * **Ledger Call:** Synchronously calls `InternalLedgerPort.debit()` to lock up the funds. If insufficient funds, throws error immediately.
2. **Execution (Saga - Virtual Thread)**:
   * Changes status to `PROCESSING`.
   * Makes HTTP call to Simulator via `CoreBankingTransferPort`.
   * *Edge Case - Timeout:* If the Simulator returns a Timeout error (Read Timeout / 504), the Saga **DOES NOT REVERSE THE DEBIT**. It pauses and relies on a scheduled status-check job or a delayed webhook.
3. **Completion (`ProcessTransferWebhookUseCase`)**:
   * Receives async webhook from Simulator.
   * If `SUCCESS`, calls `Transfer.markSuccess()`.
   * If `FAILED`, calls `Transfer.markFailed()`, and the Saga executes the **Compensating Transaction**: calls `InternalLedgerPort.credit()` to refund the merchant's wallet.

### 4.2 Retry & Resilience (`EventRetryQueueService`)
*   Uses a generic exponential backoff algorithm with jitter for outbound Simulator HTTP calls (using Resilience4j).
*   If the Simulator is completely down, the `Transfer` remains in `PENDING` and a scheduled job sweeps and retries.

---

## 5. REST API & DTOs

### 5.1 Initiate Transfer
**POST** `/api/v1/transfers`
**Headers:** `Idempotency-Key: uuid`
**Body:**
```json
{
  "amount": 500000,
  "recipientAccountNumber": "9000000001",
  "recipientBankCode": "058",
  "narration": "Payment for services"
}
```

### 5.2 Simulator Webhook Receiver
**POST** `/api/v1/transfers/webhooks/simulator`
*   **Security:** Validates `X-Simulator-Signature` HMAC.
*   **Body:** `{"event": "transfer.completed", "data": {"reference": "txn_123", "status": "SUCCESS"}}`

---

## 6. Database (MySQL 8)

Flyway: `V1__transfers__001_create_transfers.sql`

```sql
CREATE TABLE transfer_recipients (
    id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    bank_code VARCHAR(10) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    
    CONSTRAINT uq_merchant_recipient UNIQUE (merchant_id, account_number, bank_code)
);

CREATE TABLE transfers (
    id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    recipient_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    fee DECIMAL(19,4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider_reference VARCHAR(100) NULL,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    
    CONSTRAINT fk_transfer_recipient FOREIGN KEY (recipient_id) REFERENCES transfer_recipients(id)
);

CREATE INDEX idx_merchant_status ON transfers (merchant_id, status);
```
