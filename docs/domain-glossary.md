# AtlasPay — Domain Glossary (Ubiquitous Language)

This glossary defines the **precise vocabulary** for each bounded context in AtlasPay.
The same term may appear in multiple contexts but with a **different, context-specific meaning**.
When discussing or writing code, always qualify terms with their context if ambiguity is possible.

> **Rule:** If a term appears in a class name, method name, database column, API endpoint,
> Kafka topic, or any conversation — it must mean exactly what this document says it means
> in that context. No synonyms. No abbreviations. No creative reinterpretation.

---

## Shared / Cross-Context Terms

These terms have the **same meaning everywhere** in AtlasPay.

| Term | Definition |
|---|---|
| **Money** | A value object composed of a `BigDecimal` amount and a `CurrencyCode`. Never a raw number. Never a `double`. |
| **CurrencyCode** | ISO 4217 currency code (e.g., `NGN`, `USD`, `GBP`). Represented as a Java `enum`. |
| **IdempotencyKey** | A client-supplied or system-generated string (UUID v4) that uniquely identifies a mutating request. Stored as a `UNIQUE` DB column per table. |
| **CorrelationId** | A UUID that ties all log lines, Kafka messages, and HTTP calls for a single originating request together. Propagated in `X-Correlation-Id` HTTP header and Kafka message headers. |
| **DomainEvent** | An immutable Java `record` that describes something that **has already happened** in the domain. Named in past tense (e.g., `TransferCompleted`, `ChargeCreated`). |
| **AggregateRoot** | The single entry point for all mutations within a bounded context aggregate. External code may only hold a reference to the root, never to child entities. |
| **Repository** | A port (interface) in the domain layer that abstracts persistence. Implementations live in the `infrastructure` layer. |
| **UseCase** | A single, focused application-layer class or interface that orchestrates one business operation (e.g., `InitiateTransferUseCase`). One use case = one primary operation. |
| **Port** | An interface in the `application` or `domain` layer that defines what the domain needs from the outside world (`out` ports) or what it exposes to callers (`in` ports). |
| **Adapter** | An `infrastructure`-layer implementation of a `Port`. Translates between domain language and external system language (HTTP, DB, Kafka). |
| **Wallet** | An owner's balance account within the AtlasPay ledger. Every merchant and sub-account has at least one wallet. Identified by `walletId`. |
| **Timestamp** | All timestamps in AtlasPay are stored as `DATETIME(6)` UTC in MySQL and represented as `java.time.ZonedDateTime` (UTC) in Java. Never `java.util.Date`. Never `LocalDateTime` without timezone context. |

---

## Bounded Context: Identity

Module: `atlaspay-identity`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **Merchant** | A registered business entity (platform) that uses AtlasPay to power its operations and collect payments. Has a business name, RC number, and a `KycStatus`. | A `SubAccount`. |
| **SubAccount** | An end-user (individual or business) onboarded by a `Merchant`. They get virtual accounts, can make/receive payments, and use escrow under the Merchant's platform. | A `Merchant`. |
| **KycStatus** | The state of an identity verification — `UNVERIFIED`, `PENDING`, `VERIFIED`, `REJECTED`. One-way transitions only: `UNVERIFIED → PENDING → VERIFIED` or `→ REJECTED`. | A general "status" field on any other entity. |
| **BVN** | Bank Verification Number — a unique 11-digit identity number issued by the CBN to Nigerian bank customers. Verified via Dojah sandbox. | NIN. They are different identifiers. |
| **NIN** | National Identification Number — issued by NIMC. Used as an alternative/supplementary identity document. | BVN. |
| **KYC** | Know Your Customer — the process of verifying a sub-account's or merchant's identity using BVN, NIN, or business registration documents. | A single verification call. KYC is a process, not an event. |
| **AccountNameResolution** | The act of querying the bank's NIP directory to confirm the real name behind a bank account number + bank code combination. Used before creating a `TransferRecipient`. | A customer lookup. This is a bank directory query, not a system user lookup. |
| **DojahAdapter** | The infrastructure adapter that wraps HTTP calls to the Dojah API for BVN/NIN verification. | The domain `KycVerificationPort` (interface). Code outside infrastructure must never reference `DojahAdapter` directly. |

---

## Bounded Context: Accounts

Module: `atlaspay-accounts`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **VirtualAccount** | A bank account (with a real NUBAN number) issued by Anchor on behalf of a `Merchant` or `SubAccount` for the purpose of receiving inbound transfers. | A user account / login account in the Identity context. |
| **NUBAN** | Nigeria Uniform Bank Account Number — a 10-digit account number format standardised by the CBN. Represented as a value object (Java `record`), not a raw `String`. | A wallet ID. A NUBAN is an externally visible bank account number; a `walletId` is an internal ledger identifier. |
| **AccountIssuance** | The act of requesting Anchor to create a virtual account for an owner. Idempotent: calling it twice for the same owner returns the existing account. | Account creation (generic). Always say "issuance" in this context. |
| **Owner** | The `Merchant` or `SubAccount` to whom a `VirtualAccount` belongs. Referenced by `ownerId` (not `merchantId` or `subAccountId` — the accounts module does not care which type it is). | Merchant, SubAccount. The accounts module speaks in terms of `Owner`. |
| **AnchorAccountAdapter** | The infrastructure adapter for Anchor's virtual account APIs. | `AccountIssuancePort` (the domain interface). |

---

## Bounded Context: Ledger

Module: `atlaspay-ledger`

> This is the most precise context. Every term has a mathematical definition.

| Term | Definition | NOT to be confused with |
|---|---|---|
| **LedgerEntry** | A single immutable row in the `ledger_entries` table. Has a `type` (DEBIT or CREDIT), `amount` (Money), `walletId`, `referenceId`, and `idempotencyKey`. Never updated. Never deleted. | A "transaction" (in Ledger context, a transaction is a `JournalEntry`, not a `LedgerEntry`). |
| **JournalEntry** | A balanced pair (or group) of `LedgerEntry` records that represent a single financial event. `SUM(debits) == SUM(credits)` is a construction invariant. | A database transaction. In this context, "journal entry" means a bookkeeping entry, not a DB transaction. |
| **DEBIT** | An entry that **decreases** an asset wallet or **increases** a liability. A debit on a sub-account wallet means money is leaving. | The generic word "debit" — in this context, `DEBIT` specifically means the `EntryType` enum value. |
| **CREDIT** | An entry that **increases** an asset wallet or **decreases** a liability. A credit on a merchant wallet means money is arriving. | The generic word "credit." Same precision note as DEBIT. |
| **LedgerBalance** | The sum of all posted `LedgerEntry` records for a wallet, regardless of settlement state. `LedgerBalance = SUM(credits) - SUM(debits)`. Always derived, never stored. | `AvailableBalance`. |
| **AvailableBalance** | The portion of `LedgerBalance` that is settled and not held. `AvailableBalance = LedgerBalance - pending_holds`. | `LedgerBalance`. A sub-account may have a `LedgerBalance` of ₦10,000 but `AvailableBalance` of ₦7,000 if ₦3,000 is in escrow. |
| **WalletBalanceSnapshot** | A periodically checkpointed record of a wallet's balance at a point in time. Used as a performance optimization (aggregation starting point) — not the source of truth. | The actual balance. Snapshots are stale by definition; the real balance always comes from the aggregation query. |
| **ReferenceId** | The ID of the business event that caused this `LedgerEntry` (e.g., a `transferId`, `chargeId`, or `escrowId`). Not to be confused with `idempotencyKey`. | `IdempotencyKey`. They serve different purposes: `referenceId` is for traceability; `idempotencyKey` is for deduplication. |

---

## Bounded Context: Transfers

Module: `atlaspay-transfers`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **Transfer** | An outbound NIP payment from a merchant's or sub-account's wallet to an external Nigerian bank account. The aggregate root for this context. | A `Charge` (inbound collection from sub-account/external). A `Transfer` is always outbound. |
| **TransferRecipient** | A saved bank account (NUBAN + bank code + account name) that can be the target of a `Transfer`. Validated via `AccountNameResolution` before saving. | A `Merchant` or `SubAccount`. A recipient is a bank account entity, not a system user. |
| **TransferStatus** | The state machine of a `Transfer`: `PENDING → PROCESSING → SUCCESS` or `→ FAILED → REVERSED`. | A generic "status." Always use `TransferStatus` in this context, not just "status." |
| **PENDING** | Transfer has been initiated; ledger debit posted; Anchor call not yet made. | "Waiting." PENDING has a specific technical meaning: debit is already posted. |
| **PROCESSING** | Transfer has been submitted to Anchor and is awaiting bank confirmation. | "In progress" (generic). Use PROCESSING, not "in progress." |
| **SUCCESS** | Anchor confirmed the transfer was received by the destination bank. | "Done" or "complete." |
| **FAILED** | Anchor returned a failure or the transfer timed out without confirmation. A compensating credit (reversal) is posted to the ledger. | REVERSED. FAILED is the terminal failure state; REVERSED means compensation already posted. |
| **Reversal** | A compensating `LedgerEntry` (CREDIT) posted when a `Transfer` fails, returning the debited amount to the sender's wallet. | A `Refund` (Refunds belong to the Charges context). A reversal is a transfer-level compensation; a refund is a charge-level compensation. |
| **AnchorTransferAdapter** | The infrastructure adapter wrapping Anchor's transfer APIs. | `TransferGatewayPort` (the domain interface). |
| **WebhookHmacVerifier** | The component that validates Anchor's `X-Anchor-Signature` header on incoming webhook callbacks. Prevents spoofed webhook attacks. | JWT verification (different mechanism for different use case). |

---

## Bounded Context: Charges

Module: `atlaspay-charges`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **Charge** | An inbound payment collected from a sub-account or external payer (via bank transfer to a virtual account, or card). The aggregate root. | A `Transfer` (outbound). A `Charge` is always an inbound collection. |
| **ChargeStatus** | `PENDING → PROCESSING → SUCCESS → REFUNDED` or `→ FAILED`. | `TransferStatus`. Same shape, different lifecycle, different names. |
| **Refund** | A partial or full return of a `Charge` to the payer. A child entity of `Charge` (same aggregate boundary). The `Charge` enforces that total refunds never exceed the refundable balance. | A `Reversal` (belongs to Transfers context). Refunds are at the charge level; reversals are at the transfer level. |
| **RefundableAmount** | `Charge.amount - SUM(existingRefunds)`. The remaining balance eligible for refund. Enforced as a domain invariant on the `Charge` aggregate. | The original charge amount. |
| **PaymentChannel** | The mechanism used to collect the charge: `BANK_TRANSFER` (via Anchor virtual account) or `CARD` (via Paystack, future). | A `Transfer` channel. Transfers are always bank transfers. |
| **Authorization** | In the card payment lifecycle: the act of reserving funds on a card. Captured later (or released). Not applicable to bank transfer charges. | JWT authorization (completely different domain). |
| **Capture** | Converting a card authorization into a settled charge. | A bank transfer settlement. Only applicable to card channels. |

---

## Bounded Context: Subscriptions

Module: `atlaspay-subscriptions`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **Plan** | A reusable pricing template: `name`, `amount` (Money), `interval` (BillingCycle), `intervalCount`. A catalog entity (referenced by Subscriptions). | A subscription. A Plan is a template; a Subscription is an active instance. |
| **Subscription** | An active recurring billing agreement between a merchant and a sub-account. References a `Plan`. The aggregate root. | A SaaS subscription for AtlasPay itself. This is always a merchant-issued subscription to a sub-account. |
| **BillingCycle** | `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, `ANNUALLY`. A Java `enum`. | A billing date. `BillingCycle` is the frequency pattern; the actual next billing date is derived from it. |
| **SubscriptionStatus** | `ACTIVE → PAST_DUE → CANCELLED` or `→ EXPIRED`. | `ChargeStatus`. They are separate state machines. |
| **PAST_DUE** | A renewal charge attempt failed; the subscription is still active but awaiting successful payment within a grace period. | FAILED. PAST_DUE means retry is still possible; FAILED (charge-level) means that specific charge attempt failed. |
| **BillingCycleScheduler** | The Spring `@Scheduled` component that identifies subscriptions due for renewal and initiates renewal charges. Not a domain object — infrastructure. | A use case. The scheduler triggers use cases; it is not a use case itself. |
| **Proration** | Adjustment of a subscription charge when the billing period is shorter than the full cycle (e.g., plan change mid-cycle). Computed using `BigDecimal` arithmetic, never floating-point. | A discount. Proration is a mathematical adjustment, not a reduction. |

---

## Bounded Context: Escrow

Module: `atlaspay-escrow`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **EscrowHold** | A reservation of funds from a payer's wallet, held by AtlasPay until release conditions are met. The aggregate root. | A `Charge`. An escrow hold reserves funds without immediately paying them out. |
| **EscrowState** | `FUNDED → COMPLETED_PENDING_RELEASE → RELEASED` or `→ DISPUTED`. | `ChargeStatus`. Escrow has its own state machine with different semantics. |
| **FUNDED** | Funds have been charged from the payer and reserved in the escrow ledger account. Payee has not yet received them. | PROCESSING (Charges). FUNDED means the money is securely held; collection is complete. |
| **COMPLETED_PENDING_RELEASE** | The platform has confirmed the condition/errand is met. The `ClearanceWindow` countdown is now running. | SUCCESS. The escrow condition is met but the time-lock hasn't expired. |
| **RELEASED** | Funds have been paid out to the payee. Terminal state. | REFUNDED. RELEASED means the payee received funds; a refund would return funds to the payer. |
| **ClearanceWindow** | The time period (in hours) after FUNDED during which the payer must confirm receipt. After expiry, funds are automatically released. | A payment deadline. |
| **DisputeResolution** | The process of adjudicating a dispute between payer and payee during the clearance window. Out of scope for v1; funds are frozen pending manual resolution. | An automatic reversal. Disputes require human adjudication in v1. |

---

## Bounded Context: Settlement

Module: `atlaspay-settlement`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **SettlementBatch** | A group of settled charges for a merchant, aggregated for a payout cycle. The aggregate root. | A `Transfer`. A `SettlementBatch` is the accounting aggregate; the actual payout is executed as a `Transfer`. |
| **SettlementLineItem** | A single charge or credit included in a `SettlementBatch`. Child entity of `SettlementBatch`. | A `LedgerEntry`. A line item is a settlement accounting concept; a ledger entry is a bookkeeping concept. |
| **EffectiveAmount** | `totalProcessed - totalFees - deductions`. The net amount actually paid to the merchant. Computed deterministically from line items. | `totalProcessed`. Always distinguish between gross and net in settlement discussions. |
| **TotalFees** | Sum of AtlasPay transaction fees across all `SettlementLineItem`s in the batch. | A deduction. Fees are AtlasPay's income; deductions are other adjustments (chargebacks, reversals). |
| **BatchStatus** | `PENDING → PROCESSING → SUCCESS → FAILED`. Immutable once `SUCCESS`. | `ChargeStatus`. Settlement batches have their own lifecycle. |
| **SettlementWindow** | The cut-off time that determines which charges are included in a batch (e.g., T+1 settlement: all SUCCESS charges before midnight are batched the next day). | A billing cycle. Settlement windows are payout timing; billing cycles are subscription timing. |
| **Reconciliation** | The process of verifying that `EffectiveAmount` disbursed via Anchor matches the expected amount derived from ledger entries. Run after every batch. | Settlement itself. Reconciliation is the verification step; settlement is the payout step. |

---

## Bounded Context: Transaction Splits

Module: `atlaspay-transaction-splits`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **SplitConfiguration** | A named rule that defines how a charge should be split across multiple sub-accounts. The aggregate root. | A settlement calculation. Splits are applied at charge time; settlement happens later. |
| **SplitAllocation** | A single split rule within a `SplitConfiguration`: a target sub-account and either a `percentage` or a flat `Money` amount. Child entity. | A `SettlementLineItem`. Allocations define the split rule; line items are settlement accounting records. |
| **SplitType** | `PERCENTAGE` (shares must sum to 100%) or `FLAT` (amounts must sum to ≤ charge total, remainder to merchant). | A deduction type. |
| **Subaccount** | A merchant's designated recipient for a split portion (may be another AtlasPay merchant or an external bank account). | A wallet. A subaccount is a split destination identifier; a wallet is a ledger account. |

---

## Bounded Context: Transactions Query (Read Model)

Module: `atlaspay-transactions-query`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **TransactionView** | A denormalized read-model record representing a financial event (charge, transfer, refund, ledger entry) as seen from the API consumer's perspective. Not a domain object — a projection. | A `LedgerEntry`, `Charge`, or `Transfer`. The `TransactionView` unifies multiple domain objects for query convenience. |
| **UnifiedTransactionHistory** | The aggregated, searchable history of all financial events for an owner. The product of the `atlaspay-transactions-query` projection. | The ledger. The ledger is the source of truth; the unified history is a read-optimized projection. |
| **Projection** | A `TransactionView` derived by listening to domain events from Charges, Transfers, Refunds, and Ledger modules. Updated asynchronously via Kafka. | A report. A projection is a live, continuously updated read model; a report is a point-in-time query. |
| **TransactionReference** | A human-readable, unique string identifier for a transaction shown to end users and in notifications (e.g., `ATL-TXN-2024-0001234`). | Internal IDs (UUIDs). The reference is user-facing; UUIDs are system-internal. |

---

## Bounded Context: Notifications

Module: `atlaspay-notifications`

| Term | Definition | NOT to be confused with |
|---|---|---|
| **Notification** | A message dispatched to a user (Merchant or SubAccount) about a financial event. Has a `channel`, `status`, and `payload`. | A domain event. A domain event is internal system-to-system; a notification is system-to-human. |
| **Channel** | The delivery mechanism for a notification: `EMAIL`, `SMS`, `PUSH` (in-app). A Java `enum`. | A `PaymentChannel` (Charges context). Different concept, same word — always qualify with context. |
| **NotificationStatus** | `PENDING → SENT → DELIVERED → FAILED`. | `ChargeStatus` or `TransferStatus`. Notifications have their own lifecycle. |
| **NotificationTemplate** | A parameterised message template for a notification type (e.g., "Your transfer of {amount} to {recipient} was successful."). | The notification itself. Templates are configuration; notifications are instances. |

---

## Anti-Glossary — Terms We Do NOT Use

These terms are banned from code, docs, and conversation because they are ambiguous
or have a more precise equivalent.

| Banned term | Use instead | Why |
|---|---|---|
| `transaction` (unqualified) | `LedgerEntry`, `Transfer`, `Charge`, `JournalEntry` (based on context) | "Transaction" means 4 different things across contexts |
| `account` (unqualified) | `VirtualAccount`, `Wallet`, `Merchant`, `SubAccount` (based on context) | Too ambiguous — always qualify |
| `payment` (unqualified) | `Charge` (inbound) or `Transfer` (outbound) | "Payment" doesn't tell you direction |
| `amount` (raw `double`) | `Money` | Raw amounts without currency and type are meaningless |
| `status` (unqualified) | `TransferStatus`, `ChargeStatus`, `KycStatus`, etc. | Every status has a specific type |
| `user` | `Merchant` or `SubAccount` (based on context) | Too generic for a payment domain |
| `record` (verb, meaning "save") | `persist`, `post`, `save` | "Record" is also a Java keyword and a DDD concept |
| `process` (verb, meaning anything) | `initiate`, `submit`, `post`, `dispatch`, `settle` | Too vague — every use case has a precise verb |
