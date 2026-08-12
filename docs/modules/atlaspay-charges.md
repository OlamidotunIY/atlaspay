# Module Design Document — `atlaspay-charges`

> **Status:** `APPROVED`
> **Author:** Antigravity & User
> **Created:** 2026-08-12
> **Last Updated:** 2026-08-12

---

## 1. Overview
The `atlaspay-charges` module is responsible for inbound payment collection. It handles processing charges (e.g., via Card, Bank Transfer, USSD) and manages the lifecycle of refunds. It acts as the orchestration layer between the user checkout experience and the external payment gateways (Paystack, Simulator). 

---

## 2. Folder Structure

```text
atlaspay-charges/
└── src/main/java/com/atlaspay/charges/
    ├── domain/
    │   ├── model/
    │   │   ├── Charge.java                      # Aggregate Root
    │   │   ├── Refund.java                      # Child Entity of Charge
    │   │   ├── ChargeStatus.java                # Enum (PENDING, SUCCESS, FAILED, REFUNDED)
    │   │   ├── Channel.java                     # Enum (CARD, BANK_TRANSFER)
    │   │   └── RefundReason.java                # Enum
    │   ├── event/
    │   │   ├── ChargeSuccessful.java            # Record
    │   │   ├── ChargeFailed.java                # Record
    │   │   └── RefundProcessed.java             # Record
    │   ├── exception/
    │   │   └── ChargeErrorCode.java             # Enum
    │   └── repository/
    │       └── ChargeDomainRepository.java
    ├── application/
    │   ├── command/
    │   │   ├── CreateChargeCommand.java
    │   │   └── ProcessRefundCommand.java
    │   ├── query/
    │   │   ├── GetChargeQuery.java
    │   │   └── ListChargesQuery.java
    │   ├── dto/
    │   │   ├── ChargeDto.java
    │   │   ├── RefundDto.java
    │   │   └── CheckoutResponseDto.java
    │   ├── usecase/
    │   │   ├── CreateChargeUseCase.java
    │   │   ├── ProcessRefundUseCase.java
    │   │   └── HandleGatewayWebhookUseCase.java
    │   └── port/out/
    │       ├── PaymentGatewayPort.java          # Interface for external gateway
    │       └── InternalLedgerPort.java          # Call to atlaspay-ledger
    ├── infrastructure/
    │   ├── persistence/
    │   │   ├── ChargeEntity.java                # JPA Entity
    │   │   ├── RefundEntity.java                # JPA Entity (Many-to-One with Charge)
    │   │   └── ChargeJpaRepository.java
    │   ├── adapter/
    │   │   ├── paystack/
    │   │   │   └── PaystackGatewayAdapter.java  # Implements PaymentGatewayPort
    │   │   ├── simulator/
    │   │   │   └── SimulatorGatewayAdapter.java # Implements PaymentGatewayPort
    │   │   └── ledger/
    │   │       └── LedgerGrpcAdapter.java
    │   └── messaging/
    │       └── ChargeEventPublisher.java        # Outbox Kafka publisher
    └── presentation/rest/
        ├── CheckoutController.java              # Public APIs for customers
        ├── ChargeController.java                # Merchant APIs
        └── GatewayWebhookController.java        # Receives provider callbacks
```

---

## 3. Domain Layer

### 3.1 Aggregate Root: `Charge` & Child Entity: `Refund`
**Class:** `public final class Charge extends AggregateRoot<ChargeId>`

**Fields:**
*   `private final String merchantId;`
*   `private final String customerId;`
*   `private final Money amount;`
*   `private final Channel channel;`
*   `private ChargeStatus status;`
*   `private String providerReference;`
*   `private final List<Refund> refunds;`

**Methods & Invariants:**
*   `public void markSuccess(String providerRef)`
    *   `if (status != PENDING) throw new IllegalStateException();`
*   `public void processRefund(Money refundAmount, RefundReason reason)`
    *   **Core Invariant:** Refund total never exceeds the charge's refundable balance.
    *   `Money totalRefunded = refunds.stream().map(Refund::amount).reduce(Money.zero(), Money::add);`
    *   `if (totalRefunded.add(refundAmount).isGreaterThan(this.amount)) throw new BusinessRuleException(ChargeErrorCode.EXCEEDS_REFUNDABLE_BALANCE);`
    *   Adds new `Refund` to the `refunds` list.
    *   If fully refunded, sets `status = REFUNDED`.
    *   Emits `RefundProcessed` event.

---

## 4. Application Layer & Algorithms

### 4.1 Create Charge Workflow
1. **Initiation (`CreateChargeUseCase`)**:
   * Receives `CreateChargeCommand(@IdempotencyKey String idempotencyKey, Money amount, String email, Channel channel)`.
   * **Idempotency:** The DB guarantees no duplicate charges via `UNIQUE(idempotency_key)`.
   * Calls `PaymentGatewayPort.initializePayment(...)`.
   * Returns a `CheckoutResponseDto` containing the Gateway's checkout URL (e.g., Paystack's URL).
   * Saves `Charge` as `PENDING`.

### 4.2 Webhook Handling (`HandleGatewayWebhookUseCase`)
1. Receives async webhook from Gateway.
2. Validates HMAC signature (e.g., Paystack `x-paystack-signature`).
3. Loads `Charge` by `providerReference`.
4. Calls `charge.markSuccess()`.
5. **Ledger Integration (Double-Entry):**
   * Synchronously calls `InternalLedgerPort.credit()` to add funds to the Merchant's ledger available balance.
   * *Note: The ledger call uses the webhook's unique event ID as its idempotency key to prevent double-crediting if the Gateway fires the same webhook twice.*

---

## 5. REST API & DTOs

### 5.1 Initialize Checkout
**POST** `/api/v1/charges/initialize`
**Headers:** `Idempotency-Key: uuid`
**Body:**
```json
{
  "amount": 1000000, 
  "email": "customer@example.com",
  "channel": "CARD"
}
```
**Response:** `200 OK`
```json
{
  "chargeId": "chg_123",
  "authorizationUrl": "https://checkout.paystack.com/xxxx",
  "accessCode": "xxxx",
  "reference": "ref_987"
}
```

### 5.2 Process Refund
**POST** `/api/v1/charges/{chargeId}/refunds`
**Headers:** `Idempotency-Key: uuid`
**Body:**
```json
{
  "amount": 200000,
  "reason": "CUSTOMER_REQUESTED"
}
```

---

## 6. Database (MySQL 8)

Flyway: `V1__charges__001_create_charges.sql`

```sql
CREATE TABLE charges (
    id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50) NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider_reference VARCHAR(100) NULL,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE INDEX idx_merchant_status ON charges (merchant_id, status);

CREATE TABLE refunds (
    id VARCHAR(50) PRIMARY KEY,
    charge_id VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    reason VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider_refund_reference VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL,
    
    CONSTRAINT fk_refund_charge FOREIGN KEY (charge_id) REFERENCES charges(id)
);
```
