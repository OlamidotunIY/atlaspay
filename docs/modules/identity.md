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
- Merchant registration, profile management, and multi-step compliance verification (Profile, Contact, Owner, Account, Service Agreement).
- Email verification.
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
| `country` | `String` | No | ISO 3166-1 alpha-2 country code (e.g., `NG`) |
| `businessName` | `String` | No | Legal or trading name of the business |
| `firstName` | `String` | No | Account owner's first name |
| `lastName` | `String` | No | Account owner's last name |
| `email` | `EmailAddress` | No | Unique across all Merchants; used for login |
| `phone` | `PhoneNumber` | No | E.164 format |
| `hashedPassword` | `String` | No | BCrypt-hashed password; managed by Spring Security. Never exposed in responses. |
| `businessType` | `BusinessType` | No | `STARTER` (unregistered) or `REGISTERED` (has RC/CAC number) |
| `rcNumber` | `String` | Yes | CAC Registration Number; required for `REGISTERED` businesses |
| `emailVerified` | `boolean` | No | Defaults to `false`; set `true` after email verification link is clicked |
| `emailVerificationToken` | `String` | Yes | Short-lived UUID token; cleared after verification |
| `emailVerificationTokenExpiresAt` | `ZonedDateTime` | Yes | TTL for the verification token (e.g., 24 hours) |
| `complianceStatus` | `ComplianceStatus` | No | Defaults to `NOT_STARTED` |
| `complianceStep` | `ComplianceStep` | Yes | The step currently in progress; null when `NOT_STARTED` |
| `compliance` | `MerchantCompliance` | Yes | Child entity holding all compliance data; populated as steps are completed |
| `createdAt` | `ZonedDateTime` | No | UTC |
| `updatedAt` | `ZonedDateTime` | No | UTC |

**Two-phase lifecycle:**
```
Phase 1 — Email Verification:
  REGISTERED (emailVerified=false) → [click email link] → ACTIVE (emailVerified=true)
  → Test mode API keys are now active.

Phase 2 — Compliance (unlocks live mode):
  ComplianceStatus: NOT_STARTED → IN_PROGRESS → SUBMITTED → UNDER_REVIEW → APPROVED
                                                                           ↘ REJECTED
```

**Invariants:**
- Email must be unique across all Merchants.
- `REGISTERED` business type requires `rcNumber` before the OWNER compliance step can be completed.
- `emailVerificationToken` expires after 24 hours; re-sending generates a new token and invalidates the old one.
- Compliance steps must be completed in order: PROFILE → CONTACT → OWNER → ACCOUNT → SERVICE_AGREEMENT.
- Live API keys are only generated when `complianceStatus` transitions to `APPROVED`.
- `hashedPassword` is never returned in any API response.

**Domain Methods:**

| Method | Parameters | Returns | Throws | Description |
|---|---|---|---|---|
| `verifyEmail(String token)` | `token` | `void` | `BusinessRuleException(EMAIL_ALREADY_VERIFIED)`, `BusinessRuleException(EMAIL_TOKEN_INVALID_OR_EXPIRED)` | Validates token and expiry; sets `emailVerified = true`; clears token; raises `MerchantEmailVerified` |
| `regenerateEmailVerificationToken()` | — | `void` | `BusinessRuleException(EMAIL_ALREADY_VERIFIED)` | Generates a new token + expiry; raises `MerchantEmailVerificationResent` |
| `completeComplianceStep(ComplianceStep step, ...)` | Step-specific data | `void` | `BusinessRuleException(COMPLIANCE_STEP_OUT_OF_ORDER)` | Saves step data onto `MerchantCompliance`; advances `complianceStep`; raises `MerchantComplianceStepCompleted` |
| `submitCompliance()` | — | `void` | `BusinessRuleException(COMPLIANCE_NOT_ALL_STEPS_COMPLETE)` | Moves `complianceStatus` to `SUBMITTED`; raises `MerchantComplianceSubmitted` |
| `approveCompliance()` | — | `void` | `BusinessRuleException(COMPLIANCE_NOT_SUBMITTED)` | Moves status to `APPROVED`; raises `MerchantComplianceApproved` |
| `rejectCompliance(String reason)` | `reason` | `void` | `BusinessRuleException(COMPLIANCE_NOT_SUBMITTED)` | Moves status to `REJECTED`; raises `MerchantComplianceRejected` |
| `updateProfile(String businessName, PhoneNumber phone)` | fields | `void` | — | Updates mutable contact fields; raises `MerchantProfileUpdated` |

**Domain Events Raised:**

| Event | Raised When |
|---|---|
| `MerchantRegistered` | On first creation |
| `MerchantEmailVerified` | On `verifyEmail()` |
| `MerchantEmailVerificationResent` | On `regenerateEmailVerificationToken()` |
| `MerchantComplianceStepCompleted` | On `completeComplianceStep()` |
| `MerchantComplianceSubmitted` | On `submitCompliance()` |
| `MerchantComplianceApproved` | On `approveCompliance()` |
| `MerchantComplianceRejected` | On `rejectCompliance()` |
| `MerchantProfileUpdated` | On `updateProfile()` |

---

#### `MerchantCompliance` (child entity, inside Merchant aggregate boundary)

> Not an aggregate root — owned and persisted by `Merchant`. External code never holds a direct
> reference to `MerchantCompliance`; they interact through the `Merchant` aggregate.

**Step 1 — Profile fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `description` | `String` | Yes | Business description |
| `staffSize` | `StaffSize` | Yes | Enum: `ONE_TO_TEN`, `ELEVEN_TO_FIFTY`, `FIFTY_ONE_TO_TWO_HUNDRED`, `OVER_TWO_HUNDRED` |
| `industry` | `String` | Yes | e.g., `Fintech`, `E-commerce`, `Healthcare` |
| `category` | `String` | Yes | Sub-category within industry |
| `annualProjectedSalesVolume` | `Money` | Yes | In the merchant's operating currency |

**Step 2 — Contact fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `generalEmail` | `EmailAddress` | Yes | Merchant's general email |
| `supportEmail` | `EmailAddress` | Yes | Merchant's customer support email |
| `disputeEmail` | `EmailAddress` | Yes | Merchant's dispute resolution email |
| `supportPhone` | `PhoneNumber` | Yes | Merchant's customer support phone |
| `whatsappPhone` | `PhoneNumber` | Yes | Trusted WhatsApp phone number |
| `whatsappName` | `String` | Yes | Name to easily identify the WhatsApp number |
| `websiteUrl` | `String` | Yes | Optional. If supplied, must be a valid URL |
| `twitterHandle` | `String` | Yes | Twitter username |
| `facebookUsername` | `String` | Yes | Facebook username |
| `instagramHandle` | `String` | Yes | Instagram handle |
| `businessCountry` | `String` | Yes | ISO 3166-1 alpha-2 (non-editable, copied from Merchant) |
| `businessState` | `String` | Yes | State / region |
| `businessLga` | `String` | Yes | Local Government Area |
| `businessCity` | `String` | Yes | City |
| `businessStreet` | `String` | Yes | Street address |

**Step 3 — Owner fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `ownerBvn` | `String` | Yes | 11-digit BVN |
| `ownerNin` | `String` | Yes | NIN (at least one of BVN/NIN required) |
| `ownerDateOfBirth` | `LocalDate` | Yes | — |
| `ownerAddress` | `String` | Yes | Residential address |
| `ownerIdType` | `GovernmentIdType` | Yes | `PASSPORT`, `DRIVERS_LICENSE`, `VOTERS_CARD`, `NIN_SLIP` |
| `ownerIdNumber` | `String` | Yes | ID document number |

**Step 4 — Account fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `settlementBankCode` | `String` | Yes | CBN bank code |
| `settlementAccountNumber` | `String` | Yes | 10-digit NUBAN |
| `settlementAccountName` | `String` | Yes | Resolved via NIP directory (read-only after set) |

**Step 5 — Service Agreement fields:**

| Field | Type | Nullable | Notes |
|---|---|---|---|
| `agreedToTerms` | `boolean` | No | Defaults to `false`; set `true` on step completion |
| `agreementSignedAt` | `ZonedDateTime` | Yes | UTC timestamp when agreement was accepted |

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
| `BusinessType` | `STARTER`, `REGISTERED` | `STARTER` = unregistered/informal business; `REGISTERED` = has CAC/RC number |
| `ComplianceStatus` | `NOT_STARTED`, `IN_PROGRESS`, `SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED` | Replaces the old `KycStatus`. Governs live-mode access. |
| `ComplianceStep` | `PROFILE`, `CONTACT`, `OWNER`, `ACCOUNT`, `SERVICE_AGREEMENT` | The current compliance step being filled in |
| `StaffSize` | `ONE_TO_TEN`, `ELEVEN_TO_FIFTY`, `FIFTY_ONE_TO_TWO_HUNDRED`, `OVER_TWO_HUNDRED` | Used in compliance PROFILE step |
| `GovernmentIdType` | `PASSPORT`, `DRIVERS_LICENSE`, `VOTERS_CARD`, `NIN_SLIP` | Used in compliance OWNER step |
| `KeyType` | `PUBLIC`, `SECRET` | Determines display and storage strategy for an `ApiKey` |
| `ApiEnvironment` | `TEST`, `LIVE` | `LIVE` keys only generated after `ComplianceStatus` is `APPROVED` |

---

## 3. Domain Events

All events implement `DomainEvent` (shared-kernel). All are Java `record`s.

| Event Record | Raised By | Extra Fields Beyond Base | Kafka Topic |
|---|---|---|---|
| `MerchantRegistered` | `Merchant` (constructor) | `businessName`, `email`, `country`, `businessType` | `atlaspay.identity.merchant.registered` |
| `MerchantEmailVerified` | `Merchant.verifyEmail()` | — | `atlaspay.identity.merchant.email.verified` |
| `MerchantEmailVerificationResent` | `Merchant.regenerateEmailVerificationToken()` | — | `atlaspay.identity.merchant.email.verification.resent` |
| `MerchantComplianceStepCompleted` | `Merchant.completeComplianceStep()` | `step` (ComplianceStep) | `atlaspay.identity.merchant.compliance.step.completed` |
| `MerchantComplianceSubmitted` | `Merchant.submitCompliance()` | — | `atlaspay.identity.merchant.compliance.submitted` |
| `MerchantComplianceApproved` | `Merchant.approveCompliance()` | — | `atlaspay.identity.merchant.compliance.approved` |
| `MerchantComplianceRejected` | `Merchant.rejectCompliance()` | `reason` | `atlaspay.identity.merchant.compliance.rejected` |
| `MerchantProfileUpdated` | `Merchant.updateProfile()` | — | `atlaspay.identity.merchant.updated` |
| `CustomerCreated` | `Customer` (constructor) | `merchantId`, `email`, `firstName`, `lastName` | `atlaspay.identity.customer.created` |
| `CustomerProfileUpdated` | `Customer.updateProfile()` | `merchantId` | `atlaspay.identity.customer.updated` |
| `SubAccountRegistered` | `SubAccount` (constructor) | `merchantId`, `bankCode`, `accountNumber`, `accountName` | `atlaspay.identity.subaccount.registered` |
| `SubAccountDeactivated` | `SubAccount.deactivate()` | `merchantId` | `atlaspay.identity.subaccount.deactivated` |
| `ApiKeyGenerated` | `ApiKey` (constructor) | `merchantId`, `keyType`, `environment`, `prefix` | `atlaspay.identity.apikey.generated` |
| `ApiKeyRevoked` | `ApiKey.revoke()` | `merchantId`, `keyType`, `environment` | `atlaspay.identity.apikey.revoked` |

> **Note on `MerchantComplianceApproved`:** When this event is published, the application layer triggers `GenerateLiveApiKeyPairUseCase` to automatically issue the merchant's live key pair.
>
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

### `ApiKeyRepository`

| Method | Returns | Notes |
|---|---|---|
| `save(ApiKey key)` | `ApiKey` | Inherited |
| `findById(ApiKeyId id)` | `Optional<ApiKey>` | Inherited |
| `findByKeyHash(String keyHash)` | `Optional<ApiKey>` | Used by the auth filter for lookup by hashed secret key |
| `findByMerchantIdAndKeyTypeAndEnvironmentAndActiveTrue(MerchantId, KeyType, ApiEnvironment)` | `Optional<ApiKey>` | Find the one active key of a given type/env for a merchant |
| `findAllByMerchantId(MerchantId merchantId)` | `List<ApiKey>` | List all keys (active and revoked) for a merchant |

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
    String country,
    String businessName,
    String firstName,
    String lastName,
    String email,
    String phone,
    String password,
    BusinessType businessType
) {}
```

**Output:** `MerchantId`

**Happy Path:**
1. Validate all input fields via Value Object constructors.
2. Check `MerchantRepository.findByEmail()` — throw `ConflictException` if taken.
3. Hash password using BCrypt.
4. Generate `emailVerificationToken` (UUID) and `emailVerificationTokenExpiresAt` (now + 24h).
5. Create `Merchant` aggregate (raises `MerchantRegistered`).
6. `merchantRepository.save(merchant)`.
7. Publish domain events.
8. **Trigger `SendEmailVerificationUseCase`** — sends verification email to merchant.
9. Return `MerchantId` and test key pair (from `GenerateTestApiKeyPairUseCase`, chained internally).

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Email already registered | `ConflictException` | `MERCHANT_EMAIL_ALREADY_EXISTS` |
| Invalid email format | `ValidationException` | `INVALID_EMAIL_FORMAT` |
| Invalid phone format | `ValidationException` | `INVALID_PHONE_FORMAT` |

---

### `VerifyMerchantEmailUseCase`

**Type:** Command

**Input:**
```java
public record VerifyMerchantEmailCommand(String token) {}
```

**Output:** `void`

**Happy Path:**
1. Find Merchant by `emailVerificationToken` — throw `NotFoundException` if not found.
2. Delegate to `merchant.verifyEmail(token)` — validates expiry and clears token.
3. `merchantRepository.save(merchant)` → publish `MerchantEmailVerified`.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Token not found | `NotFoundException` | `EMAIL_TOKEN_NOT_FOUND` |
| Token expired | `BusinessRuleException` | `EMAIL_TOKEN_INVALID_OR_EXPIRED` |
| Email already verified | `BusinessRuleException` | `EMAIL_ALREADY_VERIFIED` |

---

### `ResendEmailVerificationUseCase`

**Type:** Command

**Input:**
```java
public record ResendEmailVerificationCommand(MerchantId merchantId) {}
```

**Output:** `void`

**Happy Path:**
1. Load merchant — throw `NotFoundException` if absent.
2. Call `merchant.regenerateEmailVerificationToken()`.
3. Save → publish `MerchantEmailVerificationResent`.
4. Trigger email send.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Email already verified | `BusinessRuleException` | `EMAIL_ALREADY_VERIFIED` |

---

### `CompleteComplianceStepUseCase`

**Type:** Command (one use case per step, or a single polymorphic one — implementation choice)

**Input (example — PROFILE step):**
```java
public record CompleteProfileStepCommand(
    MerchantId authenticatedMerchantId,
    String description,
    StaffSize staffSize,
    String industry,
    String category,
    Money annualProjectedSalesVolume
) {}
```

Similar command records exist for CONTACT, OWNER, ACCOUNT, and SERVICE_AGREEMENT steps.

**Output:** `ComplianceStatus` (the updated status after the step)

**Happy Path (OWNER step):**
1. Load merchant — throw `NotFoundException` if absent.
2. Validate step data.
3. If OWNER step: call `KycVerificationPort.verify(bvn, nin)` — fail the step if provider returns failure.
4. If ACCOUNT step: call `AccountNameResolutionPort.resolve(bankCode, accountNumber)` — fail the step if not found.
5. Call `merchant.completeComplianceStep(step, data)` — throws if out of order.
6. `merchantRepository.save(merchant)` → publish `MerchantComplianceStepCompleted`.
7. If all 5 steps complete, automatically call `merchant.submitCompliance()` → publish `MerchantComplianceSubmitted`.
8. Return updated `complianceStatus`.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Merchant not found | `NotFoundException` | `MERCHANT_NOT_FOUND` |
| Email not verified | `BusinessRuleException` | `EMAIL_VERIFICATION_REQUIRED` |
| Step completed out of order | `BusinessRuleException` | `COMPLIANCE_STEP_OUT_OF_ORDER` |
| BVN/NIN verification fails (OWNER step) | `BusinessRuleException` | `KYC_VERIFICATION_FAILED` |
| Bank account not found (ACCOUNT step) | `NotFoundException` | `BANK_ACCOUNT_NOT_FOUND` |
| Provider unavailable | `ExternalServiceException` | `KYC_PROVIDER_UNAVAILABLE` / `ACCOUNT_RESOLUTION_PROVIDER_UNAVAILABLE` |

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

### `GenerateTestApiKeyPairUseCase`

**Type:** Command (called automatically after `RegisterMerchantUseCase` succeeds)

**Input:**
```java
public record GenerateTestApiKeyPairCommand(MerchantId merchantId) {}
```

**Output:** `ApiKeyPairResult` (record containing `publicKey: String` and `secretKey: String` — the raw values shown once)

**Happy Path:**
1. Generate raw public key: `pk_test_` + 32 random alphanumeric chars.
2. Generate raw secret key: `sk_test_` + 32 random alphanumeric chars.
3. For public key: `keyHash = rawKey` (stored as-is), `displayValue = rawKey`.
4. For secret key: `keyHash = HMAC-SHA256(rawKey, serverSecret)`, `displayValue = prefix + "****" + last4`.
5. Create two `ApiKey` aggregates, save both.
6. Publish `ApiKeyGenerated` for each.
7. Return `ApiKeyPairResult(rawPublicKey, rawSecretKey)` — the only time raw secret is visible.

**Failure Cases:** None (internal system call; merchant already validated).

---

### `GenerateLiveApiKeyPairUseCase`

**Type:** Command (triggered by `MerchantComplianceApproved` event handler)

**Input:**
```java
public record GenerateLiveApiKeyPairCommand(MerchantId merchantId) {}
```

**Happy Path:** Same as `GenerateTestApiKeyPairUseCase` but with `LIVE` environment and `pk_live_` / `sk_live_` prefixes.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Merchant compliance not approved | `BusinessRuleException` | `LIVE_KEYS_REQUIRE_COMPLIANCE_APPROVED` |

---

### `RevokeApiKeyUseCase`

**Type:** Command

**Input:**
```java
public record RevokeApiKeyCommand(
    MerchantId authenticatedMerchantId,
    ApiKeyId keyId
) {}
```

**Output:** `void`

**Happy Path:**
1. Load `ApiKey` by `keyId` — throw `NotFoundException` if absent.
2. Verify `apiKey.getMerchantId().equals(authenticatedMerchantId)` — throw `NotFoundException` if mismatch (do not leak existence).
3. Call `apiKey.revoke()` — throws `BusinessRuleException` if already revoked.
4. `apiKeyRepository.save(apiKey)` → publish `ApiKeyRevoked`.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Key not found or belongs to another merchant | `NotFoundException` | `API_KEY_NOT_FOUND` |
| Key already revoked | `BusinessRuleException` | `API_KEY_ALREADY_REVOKED` |

---

### `RegenerateApiKeyUseCase`

**Type:** Command

**Input:**
```java
public record RegenerateApiKeyCommand(
    MerchantId authenticatedMerchantId,
    KeyType keyType,
    ApiEnvironment environment
) {}
```

**Output:** `String` (the new raw key — shown once)

**Happy Path:**
1. Find existing active key for this merchant/type/environment.
2. Call `existingKey.revoke()` → save.
3. Generate new key (same logic as `GenerateTestApiKeyPairUseCase`).
4. Save new key → publish events.
5. Return raw new key.

**Failure Cases:**

| Condition | Exception | Error Code |
|---|---|---|
| Attempting to regenerate LIVE key without compliance | `BusinessRuleException` | `LIVE_KEYS_REQUIRE_COMPLIANCE_APPROVED` |

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

### Authentication

All endpoints (except `POST /merchants`) require one of:
- `Authorization: Bearer sk_live_xxx` — Secret API Key (API consumers / server-to-server)
- `Authorization: Bearer eyJ...` — JWT (issued at dashboard login)

The `merchantId` is **never passed in the URL**. It is always resolved from the credential:
- **JWT:** `sub` claim = `merchantId`
- **API Key:** HMAC-SHA256(key) → lookup in `api_keys` → `merchantId`

Admin-scoped endpoints (`/admin/**`) require `ROLE_ADMIN` and do accept `merchantId` in the path.

---

### Merchant Endpoints

#### `POST /merchants` — Register a new Merchant

**Auth:** Public (no token required — this is onboarding)

**Request Body:**
```json
{
  "country": "NG",
  "businessName": "Acme Corp",
  "firstName": "John",
  "lastName": "Doe",
  "email": "admin@acmecorp.com",
  "phone": "+2348012345678",
  "password": "securePassword123!",
  "businessType": "REGISTERED"
}
```

**Response `201 Created`:**
```json
{
  "merchantId": "uuid",
  "testPublicKey": "pk_test_REDACTED_DUMMY_KEY",
  "testSecretKey": "sk_test_REDACTED_DUMMY_KEY"
}
```
> The `testSecretKey` is shown **only once**. The merchant must store it securely.
> A verification email is sent immediately.

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `400` | `INVALID_EMAIL_FORMAT` | Malformed email |
| `400` | `INVALID_PHONE_FORMAT` | Malformed phone |
| `409` | `MERCHANT_EMAIL_ALREADY_EXISTS` | Email already registered |

---

#### `GET /merchants/verify-email?token={token}` — Verify email address

**Auth:** Public (no token required — user clicks the link in their email)

**Response `200 OK`:**
```json
{ "message": "Email verified successfully. You may now sign in." }
```

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `404` | `EMAIL_TOKEN_NOT_FOUND` | Token not found |
| `422` | `EMAIL_TOKEN_INVALID_OR_EXPIRED` | Token expired |
| `422` | `EMAIL_ALREADY_VERIFIED` | Already verified |

---

#### `POST /merchants/resend-verification` — Resend verification email

**Auth:** Required (merchant must be able to sign in even with unverified email to resend)

**Response `200 OK`:**
```json
{ "message": "Verification email sent." }
```

---

#### `GET /merchants/compliance` — Get current compliance status

**Auth:** Required

**Response `200 OK`:**
```json
{
  "complianceStatus": "IN_PROGRESS",
  "currentStep": "CONTACT",
  "completedSteps": ["PROFILE"],
  "remainingSteps": ["CONTACT", "OWNER", "ACCOUNT", "SERVICE_AGREEMENT"]
}
```

---

#### `PUT /merchants/compliance/profile` — Complete PROFILE step

**Auth:** Required

**Request Body:**
```json
{
  "description": "We help SMEs accept payments online.",
  "staffSize": "ONE_TO_TEN",
  "industry": "Fintech",
  "category": "Payment Processing",
  "annualProjectedSalesVolume": { "amount": "5000000.00", "currency": "NGN" }
}
```

**Response `200 OK`:**
```json
{ "complianceStatus": "IN_PROGRESS", "completedStep": "PROFILE", "nextStep": "CONTACT" }
```

---

#### `PUT /merchants/compliance/contact` — Complete CONTACT step

**Request Body:**
```json
{
  "generalEmail": "hello@acmecorp.com",
  "supportEmail": "support@acmecorp.com",
  "disputeEmail": "disputes@acmecorp.com",
  "supportPhone": "+2348022222222",
  "whatsappPhone": "+2348022222222",
  "whatsappName": "Acme Support Team",
  "websiteUrl": "https://acmecorp.com",
  "twitterHandle": "acmecorp",
  "facebookUsername": "acmecorporation",
  "instagramHandle": "acmecorp_official",
  "state": "Lagos",
  "lga": "Ikeja",
  "city": "Ikeja",
  "street": "14 Broad Street"
}
```

---

#### `PUT /merchants/compliance/owner` — Complete OWNER step (triggers BVN/NIN verification)

**Request Body:**
```json
{
  "bvn": "12345678901",
  "nin": null,
  "dateOfBirth": "1990-05-15",
  "address": "5 Allen Avenue, Ikeja, Lagos",
  "idType": "PASSPORT",
  "idNumber": "A12345678",
  "rcNumber": "RC123456"
}
```

**Error Responses:** Include `KYC_VERIFICATION_FAILED` (422) and `KYC_PROVIDER_UNAVAILABLE` (502).

---

#### `PUT /merchants/compliance/account` — Complete ACCOUNT step (triggers NIP resolution)

**Request Body:**
```json
{
  "bankCode": "044",
  "accountNumber": "0123456789"
}
```

**Response `200 OK`:**
```json
{ "accountName": "JOHN DOE", "completedStep": "ACCOUNT", "nextStep": "SERVICE_AGREEMENT" }
```

---

#### `PUT /merchants/compliance/service-agreement` — Accept terms and complete compliance

**Request Body:**
```json
{ "agreed": true }
```

**Response `200 OK`:**
```json
{ "complianceStatus": "SUBMITTED", "message": "Compliance submitted for review." }
```

---

### Customer Endpoints

#### `POST /customers` — Create a Customer

**Auth:** Required

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
{ "customerId": "uuid" }
```

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `400` | `INVALID_EMAIL_FORMAT` | Malformed email |
| `409` | `CUSTOMER_EMAIL_ALREADY_EXISTS` | Email already exists for this Merchant |

---

#### `GET /customers` — List Customers

**Auth:** Required

**Query Params:** `page` (default 0), `size` (default 50), `email` (optional filter)

**Response `200 OK`:**
```json
{
  "content": [
    { "customerId": "uuid", "firstName": "John", "email": "john@example.com", "createdAt": "..." }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 1
}
```

---

#### `GET /customers/{customerId}` — Get a Customer

**Auth:** Required

**Response `200 OK`:** Full customer object.

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `404` | `CUSTOMER_NOT_FOUND` | Not found or belongs to another merchant |

---

### SubAccount Endpoints

#### `POST /subaccounts` — Register a SubAccount

**Auth:** Required

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
| `404` | `BANK_ACCOUNT_NOT_FOUND` | Not in NIP directory |
| `409` | `SUBACCOUNT_ALREADY_EXISTS` | Duplicate for this Merchant |
| `502` | `ACCOUNT_RESOLUTION_PROVIDER_UNAVAILABLE` | Anchor failure |

---

#### `GET /subaccounts` — List SubAccounts

**Auth:** Required

**Response `200 OK`:** Paginated list of SubAccounts.

---

#### `GET /subaccounts/{subAccountId}` — Get a SubAccount

**Auth:** Required

---

#### `DELETE /subaccounts/{subAccountId}` — Deactivate a SubAccount

**Auth:** Required

**Response `200 OK`:**
```json
{ "subAccountId": "uuid", "active": false }
```

---

### API Key Management Endpoints

#### `GET /keys` — List all API keys

**Auth:** Required

**Response `200 OK`:**
```json
{
  "keys": [
    {
      "keyId": "uuid",
      "keyType": "SECRET",
      "environment": "LIVE",
      "displayValue": "sk_live_****Ab3x",
      "active": true,
      "createdAt": "2026-08-11T20:00:00Z"
    }
  ]
}
```
> Secret keys are **never** returned in full after initial generation. Only `displayValue` (masked) is returned.

---

#### `POST /keys/regenerate` — Regenerate a key

**Auth:** Required

**Request Body:**
```json
{
  "keyType": "SECRET",
  "environment": "LIVE"
}
```

**Response `200 OK`:**
```json
{
  "keyId": "uuid",
  "rawKey": "sk_live_REDACTED_DUMMY_KEY"
}
```
> `rawKey` shown **once only**. The previous key is immediately revoked.

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `422` | `LIVE_KEYS_REQUIRE_KYC_VERIFIED` | Regenerating LIVE key before KYC |

---

#### `DELETE /keys/{keyId}` — Revoke a key

**Auth:** Required

**Response `200 OK`:**
```json
{ "keyId": "uuid", "active": false }
```

**Error Responses:**

| Status | Error Code | Condition |
|---|---|---|
| `404` | `API_KEY_NOT_FOUND` | Key not found |
| `422` | `API_KEY_ALREADY_REVOKED` | Key already revoked |

---

## 8. Database

Flyway prefix: `V1__identity__*.sql`

---

### `merchants`

| Column | MySQL Type | Nullable | Notes |
|---|---|---|---|
| `id` | `CHAR(36)` | No | UUID PK |
| `country` | `CHAR(2)` | No | ISO 3166-1 alpha-2 |
| `business_name` | `VARCHAR(200)` | No | — |
| `first_name` | `VARCHAR(100)` | No | — |
| `last_name` | `VARCHAR(100)` | No | — |
| `email` | `VARCHAR(254)` | No | — |
| `phone` | `VARCHAR(20)` | No | E.164 |
| `hashed_password` | `VARCHAR(60)` | No | BCrypt output is always 60 chars |
| `business_type` | `ENUM('STARTER','REGISTERED')` | No | — |
| `email_verified` | `TINYINT(1)` | No | Default `0` |
| `email_verification_token` | `CHAR(36)` | Yes | UUID; cleared after use |
| `email_verification_token_expires_at` | `DATETIME(6)` | Yes | UTC |
| `compliance_status` | `ENUM('NOT_STARTED','IN_PROGRESS','SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED')` | No | Default `NOT_STARTED` |
| `compliance_step` | `ENUM('PROFILE','CONTACT','OWNER','ACCOUNT','SERVICE_AGREEMENT')` | Yes | Current step in progress |
| `created_at` | `DATETIME(6)` | No | UTC |
| `updated_at` | `DATETIME(6)` | No | UTC |
| `version` | `INT` | No | Optimistic locking |

**Indexes:**

| Index | Column(s) | Type | Reason |
|---|---|---|---|
| `PRIMARY` | `id` | — | — |
| `uq_merchants_email` | `email` | UNIQUE | One account per email |
| `uq_merchants_email_verification_token` | `email_verification_token` | UNIQUE | Fast token lookup for verification |
| `idx_merchants_compliance_status` | `compliance_status` | B-tree | Filter/batch merchants by compliance state |

**Migration:** `V1__identity__create_merchants.sql`

---

### `merchant_compliance`

> Stores all compliance step data for a Merchant. One row per Merchant. Populated incrementally as steps are completed.

| Column | MySQL Type | Nullable | Notes |
|---|---|---|---|
| `merchant_id` | `CHAR(36)` | No | PK + FK → `merchants.id` (1:1 relationship) |
| `description` | `TEXT` | Yes | Profile step |
| `staff_size` | `ENUM('ONE_TO_TEN','ELEVEN_TO_FIFTY','FIFTY_ONE_TO_TWO_HUNDRED','OVER_TWO_HUNDRED')` | Yes | Profile step |
| `industry` | `VARCHAR(100)` | Yes | Profile step |
| `category` | `VARCHAR(100)` | Yes | Profile step |
| `annual_projected_sales_volume` | `DECIMAL(19,4)` | Yes | Profile step |
| `annual_projected_sales_currency` | `CHAR(3)` | Yes | Profile step |
| `general_email` | `VARCHAR(254)` | Yes | Contact step |
| `support_email` | `VARCHAR(254)` | Yes | Contact step |
| `dispute_email` | `VARCHAR(254)` | Yes | Contact step |
| `support_phone` | `VARCHAR(20)` | Yes | Contact step |
| `whatsapp_phone` | `VARCHAR(20)` | Yes | Contact step |
| `whatsapp_name` | `VARCHAR(100)` | Yes | Contact step |
| `website_url` | `VARCHAR(255)` | Yes | Contact step (optional) |
| `twitter_handle` | `VARCHAR(50)` | Yes | Contact step |
| `facebook_username` | `VARCHAR(50)` | Yes | Contact step |
| `instagram_handle` | `VARCHAR(50)` | Yes | Contact step |
| `business_country` | `CHAR(2)` | Yes | Contact step (non-editable) |
| `business_state` | `VARCHAR(100)` | Yes | Contact step |
| `business_lga` | `VARCHAR(100)` | Yes | Contact step |
| `business_city` | `VARCHAR(100)` | Yes | Contact step |
| `business_street` | `VARCHAR(255)` | Yes | Contact step |
| `owner_bvn` | `VARCHAR(11)` | Yes | Owner step (encrypted at rest) |
| `owner_nin` | `VARCHAR(11)` | Yes | Owner step (encrypted at rest) |
| `owner_date_of_birth` | `DATE` | Yes | Owner step |
| `owner_address` | `VARCHAR(500)` | Yes | Owner step |
| `owner_id_type` | `ENUM('PASSPORT','DRIVERS_LICENSE','VOTERS_CARD','NIN_SLIP')` | Yes | Owner step |
| `owner_id_number` | `VARCHAR(50)` | Yes | Owner step (encrypted at rest) |
| `rc_number` | `VARCHAR(50)` | Yes | Owner step; Required if BusinessType is REGISTERED |
| `settlement_bank_code` | `VARCHAR(10)` | Yes | Account step |
| `settlement_account_number` | `VARCHAR(10)` | Yes | Account step |
| `settlement_account_name` | `VARCHAR(200)` | Yes | Account step (NIP-resolved, read-only after set) |
| `agreed_to_terms` | `TINYINT(1)` | No | Default `0`; Service Agreement step |
| `agreement_signed_at` | `DATETIME(6)` | Yes | UTC; Service Agreement step |
| `updated_at` | `DATETIME(6)` | No | UTC |

**Indexes:** PRIMARY on `merchant_id` only (1:1 with merchants).

**Migration:** `V2__identity__create_merchant_compliance.sql`

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

**Migration:** `V3__identity__create_customers.sql`

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

**Migration:** `V4__identity__create_sub_accounts.sql`

---

### `api_keys`

| Column | MySQL Type | Nullable | Notes |
|---|---|---|---|
| `id` | `CHAR(36)` | No | UUID PK |
| `merchant_id` | `CHAR(36)` | No | FK → `merchants.id` |
| `key_type` | `ENUM('PUBLIC','SECRET')` | No | — |
| `environment` | `ENUM('TEST','LIVE')` | No | — |
| `key_hash` | `VARCHAR(64)` | No | HMAC-SHA256 hex digest (or full value for PUBLIC keys) |
| `display_value` | `VARCHAR(100)` | No | Full value for PUBLIC; masked for SECRET |
| `prefix` | `VARCHAR(10)` | No | `pk_live_`, `sk_live_`, `pk_test_`, `sk_test_` |
| `active` | `TINYINT(1)` | No | Default `1` |
| `created_at` | `DATETIME(6)` | No | UTC |
| `revoked_at` | `DATETIME(6)` | Yes | Set on revocation |

**Indexes:**

| Index | Column(s) | Type | Reason |
|---|---|---|---|
| `PRIMARY` | `id` | — | — |
| `uq_api_keys_hash` | `key_hash` | UNIQUE | Fast auth filter lookup; also prevents duplicate keys |
| `idx_api_keys_merchant` | `merchant_id` | B-tree | List keys by merchant |
| `idx_api_keys_merchant_type_env_active` | `(merchant_id, key_type, environment, active)` | B-tree | Find active key of a given type/env |

**Migration:** `V5__identity__create_api_keys.sql`

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
| `EMAIL_ALREADY_VERIFIED` | `BusinessRuleException` | 422 | Email already verified |
| `EMAIL_TOKEN_NOT_FOUND` | `NotFoundException` | 404 | Verification token not found |
| `EMAIL_TOKEN_INVALID_OR_EXPIRED` | `BusinessRuleException` | 422 | Token expired or does not match |
| `EMAIL_VERIFICATION_REQUIRED` | `BusinessRuleException` | 422 | Attempting compliance without verifying email |
| `RC_NUMBER_REQUIRED_FOR_REGISTERED_BUSINESS` | `ValidationException` | 400 | businessType is REGISTERED but rcNumber missing |
| `COMPLIANCE_STEP_OUT_OF_ORDER` | `BusinessRuleException` | 422 | Attempting a step before the previous one is done |
| `COMPLIANCE_NOT_ALL_STEPS_COMPLETE` | `BusinessRuleException` | 422 | Submitting compliance before all 5 steps done |
| `COMPLIANCE_NOT_SUBMITTED` | `BusinessRuleException` | 422 | Admin trying to approve/reject before submission |
| `KYC_VERIFICATION_FAILED` | `BusinessRuleException` | 422 | BVN/NIN check failed at Dojah |
| `BANK_ACCOUNT_NOT_FOUND` | `NotFoundException` | 404 | Account number not found in NIP directory |
| `ACCOUNT_RESOLUTION_PROVIDER_UNAVAILABLE` | `ExternalServiceException` | 502 | Anchor account-name resolution API failure |
| `API_KEY_NOT_FOUND` | `NotFoundException` | 404 | Key ID not found or belongs to another merchant |
| `API_KEY_ALREADY_REVOKED` | `BusinessRuleException` | 422 | Revoking an already-revoked key |
| `LIVE_KEYS_REQUIRE_COMPLIANCE_APPROVED` | `BusinessRuleException` | 422 | Regenerating LIVE key before compliance approved |

---

## 10. Architecture Decisions (Resolved)

> All questions below have been decided. This section is kept for audit trail.

| # | Question | Decision | Rationale |
|---|---|---|---|
| 1 | Where does password hashing / login live? | **`atlaspay-app`** | Spring Security configuration belongs at the composition root. `atlaspay-identity` owns registration and profile only — it stores the `hashedPassword` field but never performs authentication itself. |
| 2 | Do Customers need their own login? | **No — records only** | Customers are data records managed by the Merchant via API key. They have no login, no JWT, and no direct API access. |
| 3 | SubAccount deactivation cascade to SplitConfigurations? | **Yes — auto-removed** | When a SubAccount is deactivated, `atlaspay-transaction-splits` must remove it from any active `SplitConfiguration`. The identity module publishes `SubAccountDeactivated`; the splits module listens and handles cleanup. |
| 4 | Separate use case classes per compliance step? | **Yes — one class per step** | `CompleteProfileStepUseCase`, `CompleteContactStepUseCase`, `CompleteOwnerStepUseCase`, `CompleteAccountStepUseCase`, `CompleteServiceAgreementStepUseCase`. Each has its own command, validation rules, and external port calls. Easier to test and extend. |
| 5 | `merchant_compliance` — `@Embedded` or `@OneToOne`? | **`@OneToOne` separate entity** | 27 compliance columns in the `merchants` table would make it very wide. A separate `merchant_compliance` table with a lazy-loaded `@OneToOne` keeps `Merchant` lean and avoids loading compliance data on every merchant read. |
| 6 | Email verification — redirect or JSON? | **Redirect to dashboard URL** | The verification link is clicked by a human in a browser from their email client. Returning JSON is useless. The endpoint redirects to `{dashboardBaseUrl}/email-verified?status=success` on success, or `?status=error&code={errorCode}` on failure. |

