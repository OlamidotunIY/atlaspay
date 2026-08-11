# Module Design Document — `atlaspay-identity`

> **Status:** `DRAFT`
> **Author:** Olamidotun
> **Created:** 2026-08-11
> **Last Updated:** 2026-08-11

---

## 1. Overview

### 1.1 Purpose
The Identity module is the authentication and registration authority for AtlasPay. It owns the
lifecycle of `Merchant`s (platform businesses), `Customer`s (the end-users a Merchant transacts
with), and `SubAccount`s (bank account routing entities used for split payments). It is the first
module every other part of the system talks to — no financial operation can happen without a
verified Merchant and, optionally, a linked Customer.

### 1.2 Scope — What This Module Does
- Merchant registration, profile management, and KYC verification.
- Customer creation, retrieval, and management on behalf of a Merchant.
- SubAccount registration (split-payment bank account routing).
- BVN / NIN identity verification via the Dojah sandbox.
- Account-name resolution (NIP directory lookup) before a TransferRecipient is saved.
- Issuing and managing `UserId` and `MerchantId` identities referenced by other modules.

### 1.3 Out of Scope — What This Module Does NOT Do
- **Virtual account issuance** — that is `atlaspay-accounts` (backed by Anchor).
- **Authentication / JWT issuance** — handled in `atlaspay-app` (Spring Security config).
- **Payment collection or transfers** — `atlaspay-charges`, `atlaspay-transfers`.
- **Split-payment logic** — `atlaspay-transaction-splits` consumes SubAccount IDs; it does not create them.
- **Customer billing or subscription management** — `atlaspay-subscriptions`.

### 1.4 Dependencies

| Dependency | Type | Reason |
|---|---|---|
| `atlaspay-shared-kernel` | Internal module | `Money`, `AggregateRoot`, `DomainEvent`, `ErrorCode`, exception hierarchy, `UserId`, `MerchantId` |
| Dojah (dojah.io) | External HTTP | BVN/NIN KYC verification (sandbox) |
| Anchor (getanchor.co) | External HTTP | Account-name resolution (NIP directory) |

---

## 2. Domain Model

### 2.1 Aggregate Roots

---

#### `Merchant`

**Identity:** `MerchantId` (in `atlaspay-shared-kernel`, `com.atlaspay.shared.domain.id`)

**Fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `id` | `MerchantId` | No | UUID, generated on creation |
| `businessName` | `String` | No | Legal business name |
| `email` | `EmailAddress` | No | Unique across all Merchants |
| `phone` | `PhoneNumber` | No | E.164 format |
| `rcNumber` | `String` | Yes | CAC Registration Number; required for KYC |
| `kycStatus` | `KycStatus` | No | Defaults to `UNVERIFIED` |
| `createdAt` | `ZonedDateTime` | No | UTC |
| `updatedAt` | `ZonedDateTime` | No | UTC |

**State Machine:**
```
UNVERIFIED → PENDING → VERIFIED
                     ↘ REJECTED
```
Transitions are one-way. A `VERIFIED` merchant cannot be sent back to `PENDING`. A `REJECTED`
merchant must re-register (or appeal via admin tooling — out of scope for v1).

**Invariants:**
- Email must be unique across all Merchants.
- `KycStatus` transitions can only move forward; no downgrade of `VERIFIED`.
- RC Number is required before KYC can be initiated.

**Domain Methods:**

| Method | Parameters | Returns | Throws | Description |
|---|---|---|---|---|
| `initiateKyc()` | — | `void` | `BusinessRuleException(KYC_ALREADY_IN_PROGRESS)` | Moves `UNVERIFIED → PENDING`; raises `MerchantKycInitiated` |
| `markKycVerified()` | — | `void` | `BusinessRuleException(KYC_ALREADY_VERIFIED)` | Moves `PENDING → VERIFIED`; raises `MerchantKycVerified` |
| `markKycRejected(String reason)` | `reason` | `void` | `BusinessRuleException(KYC_ALREADY_VERIFIED)` | Moves `PENDING → REJECTED`; raises `MerchantKycRejected` |
| `updateProfile(String businessName, PhoneNumber phone)` | `businessName`, `phone` | `void` | `ValidationException` | Updates mutable profile fields |

**Domain Events Raised:**

| Event | Raised When |
|---|---|
| `MerchantRegistered` | On first creation |
| `MerchantKycInitiated` | On `initiateKyc()` |
| `MerchantKycVerified` | On `markKycVerified()` |
| `MerchantKycRejected` | On `markKycRejected()` |

---

#### `Customer`

**Identity:** `CustomerId` (defined in `atlaspay-shared-kernel`, `com.atlaspay.shared.domain.id`)

**Fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `id` | `CustomerId` | No | UUID |
| `merchantId` | `MerchantId` | No | The Merchant this customer belongs to |
| `firstName` | `String` | No | — |
| `lastName` | `String` | No | — |
| `email` | `EmailAddress` | No | Unique per Merchant (not globally) |
| `phone` | `PhoneNumber` | Yes | Optional |
| `metadata` | `Map<String, String>` | Yes | Arbitrary key-value pairs for Merchant use |
| `createdAt` | `ZonedDateTime` | No | UTC |
| `updatedAt` | `ZonedDateTime` | No | UTC |

**Invariants:**
- `email` must be unique within the scope of a single `Merchant` (two different Merchants may
  have customers with the same email — they are different Customer identities).
- A Customer always belongs to exactly one Merchant.

**Domain Methods:**

| Method | Parameters | Returns | Throws | Description |
|---|---|---|---|---|
| `updateProfile(String firstName, String lastName, PhoneNumber phone)` | fields | `void` | `ValidationException` | Updates mutable profile fields; raises `CustomerProfileUpdated` |
| `updateMetadata(Map<String, String> metadata)` | `metadata` | `void` | — | Replaces metadata map entirely |

**Domain Events Raised:**

| Event | Raised When |
|---|---|
| `CustomerCreated` | On first creation |
| `CustomerProfileUpdated` | On `updateProfile()` |

---

#### `SubAccount`

**Identity:** `SubAccountId` (defined in `atlaspay-shared-kernel`, `com.atlaspay.shared.domain.id`)

> **Important:** A `SubAccount` is NOT a user identity. It is a bank account routing entity
> used exclusively for split-payment configuration. It has no login, no KYC, and no Customer
> relationship. See `atlaspay-transaction-splits` for how SubAccounts are used in splits.

**Fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `id` | `SubAccountId` | No | UUID |
| `merchantId` | `MerchantId` | No | The Merchant who owns this sub-account |
| `bankCode` | `String` | No | CBN bank code (e.g., `044` for Access Bank) |
| `accountNumber` | `String` | No | 10-digit NUBAN |
| `accountName` | `String` | No | Resolved via NIP directory before save |
| `description` | `String` | Yes | Optional human-readable label |
| `active` | `boolean` | No | Defaults to `true`; can be deactivated |
| `createdAt` | `ZonedDateTime` | No | UTC |

**Invariants:**
- `accountNumber` must be validated via NIP account-name resolution before a SubAccount is saved.
- `accountNumber` + `bankCode` must be unique per Merchant (no duplicate sub-accounts).

**Domain Methods:**

| Method | Parameters | Returns | Throws | Description |
|---|---|---|---|---|
| `deactivate()` | — | `void` | `BusinessRuleException(SUBACCOUNT_ALREADY_INACTIVE)` | Sets `active = false`; raises `SubAccountDeactivated` |

**Domain Events Raised:**

| Event | Raised When |
|---|---|
| `SubAccountRegistered` | On first creation |
| `SubAccountDeactivated` | On `deactivate()` |

---

### 2.2 Value Objects

#### `EmailAddress`

| Field | Type | Validation Rule |
|---|---|---|
| `value` | `String` | Non-null, non-blank; matches RFC 5322 email regex; max 254 chars |

> Implemented as a Java `record`. Throws `ValidationException(INVALID_EMAIL_FORMAT)`.

---

#### `PhoneNumber`

| Field | Type | Validation Rule |
|---|---|---|
| `value` | `String` | Non-null; E.164 format (e.g., `+2348012345678`); 7–15 digits after `+` |

> Implemented as a Java `record`. Throws `ValidationException(INVALID_PHONE_FORMAT)`.

---

### 2.3 Enums

| Enum | Values | Notes |
|---|---|---|
| `KycStatus` | `UNVERIFIED`, `PENDING`, `VERIFIED`, `REJECTED` | Belongs to `identity` domain package. Transition guards in `Merchant` aggregate. |

---

## 3. Domain Events

All events implement `DomainEvent` (shared-kernel). All are Java `record`s.

| Event Record | Raised By | Extra Fields Beyond Base | Kafka Topic |
|---|---|---|---|
| `MerchantRegistered` | `Merchant` (constructor) | `businessName`, `email` | `atlaspay.identity.merchant.registered` |
| `MerchantKycInitiated` | `Merchant.initiateKyc()` | — | `atlaspay.identity.merchant.kyc.initiated` |
| `MerchantKycVerified` | `Merchant.markKycVerified()` | — | `atlaspay.identity.merchant.kyc.verified` |
| `MerchantKycRejected` | `Merchant.markKycRejected()` | `reason` | `atlaspay.identity.merchant.kyc.rejected` |
| `CustomerCreated` | `Customer` (constructor) | `merchantId`, `email`, `firstName`, `lastName` | `atlaspay.identity.customer.created` |
| `CustomerProfileUpdated` | `Customer.updateProfile()` | `merchantId` | `atlaspay.identity.customer.updated` |
| `SubAccountRegistered` | `SubAccount` (constructor) | `merchantId`, `bankCode`, `accountNumber`, `accountName` | `atlaspay.identity.subaccount.registered` |
| `SubAccountDeactivated` | `SubAccount.deactivate()` | `merchantId` | `atlaspay.identity.subaccount.deactivated` |

> Base fields on every event: `eventId`, `aggregateId`, `occurredAt`, `correlationId`.

---

## 4. Repository Ports

All ports extend `Repository<T, ID>` from `atlaspay-shared-kernel`.
Implementations live in `infrastructure/persistence/`.

---

### `MerchantRepository`

| Method | Returns | Notes |
|---|---|---|
| `save(Merchant merchant)` | `Merchant` | Inherited |
| `findById(MerchantId id)` | `Optional<Merchant>` | Inherited |
| `existsById(MerchantId id)` | `boolean` | Inherited |
| `findByEmail(EmailAddress email)` | `Optional<Merchant>` | Used for duplicate email check on registration |

---

### `CustomerRepository`

| Method | Returns | Notes |
|---|---|---|
| `save(Customer customer)` | `Customer` | Inherited |
| `findById(CustomerId id)` | `Optional<Customer>` | Inherited |
| `existsById(CustomerId id)` | `boolean` | Inherited |
| `findByMerchantIdAndEmail(MerchantId merchantId, EmailAddress email)` | `Optional<Customer>` | Unique check scoped per Merchant |
| `findAllByMerchantId(MerchantId merchantId, Pageable pageable)` | `Page<Customer>` | List customers for a Merchant |

---

### `SubAccountRepository`

| Method | Returns | Notes |
|---|---|---|
| `save(SubAccount subAccount)` | `SubAccount` | Inherited |
| `findById(SubAccountId id)` | `Optional<SubAccount>` | Inherited |
| `existsById(SubAccountId id)` | `boolean` | Inherited |
| `findByMerchantIdAndAccountNumberAndBankCode(MerchantId, String, String)` | `Optional<SubAccount>` | Duplicate sub-account check |
| `findAllByMerchantId(MerchantId merchantId, Pageable pageable)` | `Page<SubAccount>` | List sub-accounts for a Merchant |

---

## 5. Use Cases (Application Layer)

All extend `BaseUseCase<Input, Output>` from shared-kernel.
All use cases are `@Transactional` at their boundary.
Domain events are published via `DomainEventPublisher` after `repository.save()`.

---

### `RegisterMerchantUseCase`

**Type:** Command

**Input:**
```java
public record RegisterMerchantCommand(
    String idempotencyKey,
    String businessName,
    String email,
    String phone,
    String rcNumber         // optional at registration; required before KYC
) {}
```

**Output:** `MerchantId`

**Happy Path:**
1. Validate all input fields via Value Object constructors.
2. Check `MerchantRepository.findByEmail()` — throw `ConflictException` if taken.
3. Create `Merchant` aggregate (internally calls `registerEvent(new MerchantRegistered(...))`).
4. `merchantRepository.save(merchant)`.
5. `merchant.pullDomainEvents()` → `domainEventPublisher.publish(each)`.
6. Return `merchant.getId()`.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Email already registered | `ConflictException` | `MERCHANT_EMAIL_ALREADY_EXISTS` |
| Invalid email format | `ValidationException` | `INVALID_EMAIL_FORMAT` |
| Invalid phone format | `ValidationException` | `INVALID_PHONE_FORMAT` |

---

### `VerifyMerchantKycUseCase`

**Type:** Command

**Input:**
```java
public record VerifyMerchantKycCommand(
    String idempotencyKey,
    MerchantId merchantId,
    String bvn,            // optional — one of bvn or nin required
    String nin             // optional — one of bvn or nin required
) {}
```

**Output:** `KycStatus`

**Happy Path:**
1. Load merchant via `MerchantRepository.findById()` — throw `NotFoundException` if absent.
2. Confirm `kycStatus == UNVERIFIED` — throw `BusinessRuleException` if already pending/verified.
3. Call `KycVerificationPort.verify(bvn, nin)` — throws `ExternalServiceException` on provider failure.
4. If verification passes: `merchant.initiateKyc()` then `merchant.markKycVerified()`.
5. If verification fails: `merchant.initiateKyc()` then `merchant.markKycRejected(reason)`.
6. `merchantRepository.save(merchant)` → publish events.
7. Return updated `kycStatus`.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Merchant not found | `NotFoundException` | `MERCHANT_NOT_FOUND` |
| KYC already verified | `BusinessRuleException` | `KYC_ALREADY_VERIFIED` |
| KYC already in progress | `BusinessRuleException` | `KYC_ALREADY_IN_PROGRESS` |
| Neither BVN nor NIN supplied | `ValidationException` | `KYC_IDENTITY_DOCUMENT_REQUIRED` |
| Dojah API failure | `ExternalServiceException` | `KYC_PROVIDER_UNAVAILABLE` |

---

### `CreateCustomerUseCase`

**Type:** Command

**Input:**
```java
public record CreateCustomerCommand(
    String idempotencyKey,
    MerchantId merchantId,
    String firstName,
    String lastName,
    String email,
    String phone,           // optional
    Map<String, String> metadata  // optional
) {}
```

**Output:** `CustomerId`

**Happy Path:**
1. Load merchant — throw `NotFoundException` if absent.
2. Check `CustomerRepository.findByMerchantIdAndEmail()` — throw `ConflictException` if taken.
3. Create `Customer` aggregate.
4. `customerRepository.save(customer)` → publish `CustomerCreated`.
5. Return `customer.getId()`.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Merchant not found | `NotFoundException` | `MERCHANT_NOT_FOUND` |
| Email already exists for this Merchant | `ConflictException` | `CUSTOMER_EMAIL_ALREADY_EXISTS` |
| Invalid email format | `ValidationException` | `INVALID_EMAIL_FORMAT` |

---

### `RegisterSubAccountUseCase`

**Type:** Command

**Input:**
```java
public record RegisterSubAccountCommand(
    String idempotencyKey,
    MerchantId merchantId,
    String bankCode,
    String accountNumber,
    String description      // optional
) {}
```

**Output:** `SubAccountId`

**Happy Path:**
1. Load merchant — throw `NotFoundException` if absent.
2. Call `AccountNameResolutionPort.resolve(bankCode, accountNumber)` to confirm the account exists and retrieve `accountName`.
3. Check for duplicate via `SubAccountRepository.findByMerchantIdAndAccountNumberAndBankCode()`.
4. Create `SubAccount` aggregate with resolved `accountName`.
5. `subAccountRepository.save(subAccount)` → publish `SubAccountRegistered`.
6. Return `subAccount.getId()`.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Merchant not found | `NotFoundException` | `MERCHANT_NOT_FOUND` |
| Account number not found in NIP directory | `NotFoundException` | `BANK_ACCOUNT_NOT_FOUND` |
| Duplicate sub-account for this Merchant | `ConflictException` | `SUBACCOUNT_ALREADY_EXISTS` |
| NIP resolution failure | `ExternalServiceException` | `ACCOUNT_RESOLUTION_PROVIDER_UNAVAILABLE` |

---

## 6. Outbound Ports (External Dependencies)

---

### `KycVerificationPort`

Package: `application/port/out/`

```java
public interface KycVerificationPort {
    KycVerificationResult verify(String bvn, String nin);
}
```

| Method | Parameters | Returns | Notes |
|---|---|---|---|
| `verify(bvn, nin)` | `String bvn`, `String nin` | `KycVerificationResult` | At least one of bvn/nin must be non-null. Result has `passed: boolean` and `failureReason: String`. |

**Adapter:** `DojahKycAdapter` in `infrastructure/adapter/dojah/`

---

### `AccountNameResolutionPort`

Package: `application/port/out/`

```java
public interface AccountNameResolutionPort {
    String resolve(String bankCode, String accountNumber);
}
```

| Method | Parameters | Returns | Notes |
|---|---|---|---|
| `resolve(bankCode, accountNumber)` | `String`, `String` | `String accountName` | Throws `NotFoundException(BANK_ACCOUNT_NOT_FOUND)` if not found. Throws `ExternalServiceException` on provider failure. |

**Adapter:** `AnchorAccountNameAdapter` in `infrastructure/adapter/anchor/`

---

## 7. REST API Surface

Base path: `/api/v1`

Authentication: `Bearer JWT` (required on all endpoints unless noted)

---

### `POST /merchants`

**Description:** Register a new Merchant.

**Auth:** Public (no JWT required — this is the registration endpoint).

**Request Body:**
```json
{
  "businessName": "Acme Corp",
  "email": "admin@acmecorp.com",
  "phone": "+2348012345678",
  "rcNumber": "RC123456"
}
```

| Field | Required | Notes |
|---|---|---|
| `businessName` | Yes | 2–200 chars |
| `email` | Yes | Valid email |
| `phone` | Yes | E.164 format |
| `rcNumber` | No | Can be supplied later before KYC |

**Response `201 Created`:**
```json
{
  "merchantId": "uuid"
}
```

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `400` | `INVALID_EMAIL_FORMAT` | Malformed email |
| `400` | `INVALID_PHONE_FORMAT` | Malformed phone |
| `409` | `MERCHANT_EMAIL_ALREADY_EXISTS` | Email already registered |

---

### `POST /merchants/{merchantId}/kyc/verify`

**Description:** Initiate and complete KYC verification for a Merchant.

**Request Body:**
```json
{
  "bvn": "12345678901",
  "nin": null
}
```

**Response `200 OK`:**
```json
{
  "merchantId": "uuid",
  "kycStatus": "VERIFIED"
}
```

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `404` | `MERCHANT_NOT_FOUND` | Merchant not found |
| `422` | `KYC_ALREADY_VERIFIED` | KYC already completed |
| `422` | `KYC_ALREADY_IN_PROGRESS` | KYC already pending |
| `400` | `KYC_IDENTITY_DOCUMENT_REQUIRED` | No BVN or NIN supplied |
| `502` | `KYC_PROVIDER_UNAVAILABLE` | Dojah API failure |

---

### `GET /merchants/{merchantId}`

**Response `200 OK`:**
```json
{
  "merchantId": "uuid",
  "businessName": "Acme Corp",
  "email": "admin@acmecorp.com",
  "phone": "+2348012345678",
  "kycStatus": "VERIFIED",
  "createdAt": "2026-08-11T20:00:00Z"
}
```

---

### `POST /merchants/{merchantId}/customers`

**Description:** Create a Customer for a Merchant.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "+2348011111111",
  "metadata": { "plan": "gold" }
}
```

**Response `201 Created`:**
```json
{
  "customerId": "uuid"
}
```

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `404` | `MERCHANT_NOT_FOUND` | Merchant not found |
| `400` | `INVALID_EMAIL_FORMAT` | Malformed email |
| `409` | `CUSTOMER_EMAIL_ALREADY_EXISTS` | Email already exists for this Merchant |

---

### `GET /merchants/{merchantId}/customers`

**Description:** List all Customers for a Merchant (paginated).

**Query Params:** `page` (default 0), `size` (default 20)

**Response `200 OK`:**
```json
{
  "content": [
    { "customerId": "uuid", "firstName": "John", "email": "john@example.com" }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
```

---

### `GET /merchants/{merchantId}/customers/{customerId}`

**Response `200 OK`:** Full customer object.

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `404` | `MERCHANT_NOT_FOUND` | Merchant not found |
| `404` | `CUSTOMER_NOT_FOUND` | Customer not found |

---

### `POST /merchants/{merchantId}/subaccounts`

**Description:** Register a bank account as a SubAccount for split payments.

**Request Body:**
```json
{
  "bankCode": "044",
  "accountNumber": "0123456789",
  "description": "Marketing team account"
}
```

**Response `201 Created`:**
```json
{
  "subAccountId": "uuid",
  "accountName": "JOHN DOE"
}
```

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `404` | `MERCHANT_NOT_FOUND` | Merchant not found |
| `404` | `BANK_ACCOUNT_NOT_FOUND` | Account not found in NIP directory |
| `409` | `SUBACCOUNT_ALREADY_EXISTS` | Duplicate account for this Merchant |
| `502` | `ACCOUNT_RESOLUTION_PROVIDER_UNAVAILABLE` | Anchor API failure |

---

### `GET /merchants/{merchantId}/subaccounts`

**Response `200 OK`:** Paginated list of SubAccounts for the Merchant.

---

## 8. Database

Flyway prefix: `V1__identity__*.sql`

---

### `merchants`

| Column | MySQL Type | Nullable | Notes |
|---|---|---|---|
| `id` | `CHAR(36)` | No | UUID PK |
| `business_name` | `VARCHAR(200)` | No | — |
| `email` | `VARCHAR(254)` | No | — |
| `phone` | `VARCHAR(20)` | No | E.164 |
| `rc_number` | `VARCHAR(50)` | Yes | — |
| `kyc_status` | `ENUM('UNVERIFIED','PENDING','VERIFIED','REJECTED')` | No | Default `UNVERIFIED` |
| `created_at` | `DATETIME(6)` | No | UTC |
| `updated_at` | `DATETIME(6)` | No | UTC |
| `version` | `INT` | No | Optimistic locking |

**Indexes:**

| Index | Column(s) | Type | Reason |
|---|---|---|---|
| `PRIMARY` | `id` | — | — |
| `uq_merchants_email` | `email` | UNIQUE | One account per email |
| `idx_merchants_kyc_status` | `kyc_status` | B-tree | Filter merchants by KYC state |

**Migration:** `V1__identity__create_merchants.sql`

---

### `customers`

| Column | MySQL Type | Nullable | Notes |
|---|---|---|---|
| `id` | `CHAR(36)` | No | UUID PK |
| `merchant_id` | `CHAR(36)` | No | FK → `merchants.id` |
| `first_name` | `VARCHAR(100)` | No | — |
| `last_name` | `VARCHAR(100)` | No | — |
| `email` | `VARCHAR(254)` | No | — |
| `phone` | `VARCHAR(20)` | Yes | E.164 |
| `metadata` | `JSON` | Yes | Arbitrary key-value store |
| `created_at` | `DATETIME(6)` | No | UTC |
| `updated_at` | `DATETIME(6)` | No | UTC |
| `version` | `INT` | No | Optimistic locking |

**Indexes:**

| Index | Column(s) | Type | Reason |
|---|---|---|---|
| `PRIMARY` | `id` | — | — |
| `uq_customers_merchant_email` | `(merchant_id, email)` | UNIQUE | Email unique per Merchant |
| `idx_customers_merchant_id` | `merchant_id` | B-tree | List customers by merchant |

**Migration:** `V2__identity__create_customers.sql`

---

### `sub_accounts`

| Column | MySQL Type | Nullable | Notes |
|---|---|---|---|
| `id` | `CHAR(36)` | No | UUID PK |
| `merchant_id` | `CHAR(36)` | No | FK → `merchants.id` |
| `bank_code` | `VARCHAR(10)` | No | CBN bank code |
| `account_number` | `VARCHAR(10)` | No | 10-digit NUBAN |
| `account_name` | `VARCHAR(200)` | No | NIP-resolved name |
| `description` | `VARCHAR(255)` | Yes | Human-readable label |
| `active` | `TINYINT(1)` | No | Default `1` |
| `created_at` | `DATETIME(6)` | No | UTC |
| `version` | `INT` | No | Optimistic locking |

**Indexes:**

| Index | Column(s) | Type | Reason |
|---|---|---|---|
| `PRIMARY` | `id` | — | — |
| `uq_subaccounts_merchant_bank_account` | `(merchant_id, bank_code, account_number)` | UNIQUE | No duplicate sub-accounts per Merchant |
| `idx_subaccounts_merchant_id` | `merchant_id` | B-tree | List sub-accounts by merchant |

**Migration:** `V3__identity__create_sub_accounts.sql`

---

## 9. Error Codes

All defined in `IdentityErrorCode` enum (`atlaspay-identity`, `domain/exception/`).

| Error Code | Exception Type | HTTP Status | When Thrown |
|---|---|---|---|
| `MERCHANT_NOT_FOUND` | `NotFoundException` | 404 | Merchant ID does not exist |
| `MERCHANT_EMAIL_ALREADY_EXISTS` | `ConflictException` | 409 | Email already registered as a Merchant |
| `CUSTOMER_NOT_FOUND` | `NotFoundException` | 404 | Customer ID does not exist |
| `CUSTOMER_EMAIL_ALREADY_EXISTS` | `ConflictException` | 409 | Email already used by another Customer of the same Merchant |
| `SUBACCOUNT_NOT_FOUND` | `NotFoundException` | 404 | SubAccount ID does not exist |
| `SUBACCOUNT_ALREADY_EXISTS` | `ConflictException` | 409 | Same bank account already registered for this Merchant |
| `SUBACCOUNT_ALREADY_INACTIVE` | `BusinessRuleException` | 422 | Deactivating an already-inactive SubAccount |
| `KYC_ALREADY_VERIFIED` | `BusinessRuleException` | 422 | KYC is already in `VERIFIED` state |
| `KYC_ALREADY_IN_PROGRESS` | `BusinessRuleException` | 422 | KYC is already `PENDING` |
| `KYC_IDENTITY_DOCUMENT_REQUIRED` | `ValidationException` | 400 | No BVN or NIN supplied for verification |
| `KYC_PROVIDER_UNAVAILABLE` | `ExternalServiceException` | 502 | Dojah API returned an error or timed out |
| `BANK_ACCOUNT_NOT_FOUND` | `NotFoundException` | 404 | Account number not found in NIP directory |
| `ACCOUNT_RESOLUTION_PROVIDER_UNAVAILABLE` | `ExternalServiceException` | 502 | Anchor account-name resolution API failure |

---

## 10. Open Questions / Decisions Pending

- [ ] Should password hashing / login live in `atlaspay-identity` or `atlaspay-app`? Current lean: `atlaspay-app` (Spring Security layer) with `atlaspay-identity` owning only the profile/registration side.
- [ ] Do Customers need their own login (i.e., can a Customer authenticate to the AtlasPay API), or are they purely data records managed by the Merchant via API keys?
- [ ] Should SubAccount deactivation cascade to remove them from active `SplitConfiguration`s in `atlaspay-transaction-splits`, or is that enforced at split-time?
