# AtlasPay — Persistence Design Reference (atlaspay-persistence)

> Infrastructure adapter implementing core repository interfaces via JPA. Complements docs/DESIGN.md (core domain model).

## Conventions

- Maps every core aggregate to a JPA entity class (separate classes, no methods)
- Mapper classes handle bidirectional conversion between core aggregates and JPA entities
- Repository adapter implementations wrap Spring Data `JpaRepository`
- Outbox pattern for guaranteed domain event delivery
- Flyway for schema migrations

## Module Structure

Package-by-feature mirroring `atlaspay-core`:

```
persistence/
  identity/           # Identity & Onboarding domain persistence
    CompanyJpaEntity
    CompanyMapper
    JpaCompanyRepository
    CustomerJpaEntity
    CustomerMapper
    JpaCustomerRepository
    KycCaseJpaEntity
    KycCaseMapper
    JpaKycCaseRepository
    KycEvaluationSagaJpaEntity
    KycEvaluationSagaMapper
    JpaKycEvaluationSagaRepository
  accounts/           # Accounts domain persistence
    AccountJpaEntity
    AccountMapper
    JpaAccountRepository
  ledger/             # Ledger domain persistence
    JournalEntryJpaEntity
    JournalEntryMapper
    JpaJournalEntryRepository
    LedgerLineJpaEntity      # embedded collection within JournalEntry
  transfers/          # Transfers domain persistence
    TransferJpaEntity        # sealed hierarchy base
    InternalTransferJpaEntity
    InboundExternalTransferJpaEntity
    OutboundExternalTransferJpaEntity
    TransferMapper
    JpaTransferRepository
    TransferSagaJpaEntity
    TransferSagaMapper
    JpaTransferSagaRepository
  cards/              # Cards domain persistence
    CardJpaEntity
    CardMapper
    JpaCardRepository
    DisputeJpaEntity
    DisputeMapper
    JpaDisputeRepository
    ChargebackSagaJpaEntity
    ChargebackSagaMapper
    JpaChargebackSagaRepository
  limits/             # Limits domain persistence
    LimitPolicyJpaEntity
    LimitPolicyMapper
    JpaLimitPolicyRepository
  access/             # Platform Access domain persistence
    ApiKeyJpaEntity
    ApiKeyMapper
    JpaApiKeyRepository
    WebhookSubscriptionJpaEntity
    WebhookSubscriptionMapper
    JpaWebhookSubscriptionRepository
    WebhookDeliveryJpaEntity
    WebhookDeliveryMapper
    JpaWebhookDeliveryRepository
  outbox/             # Transactional outbox infrastructure
    OutboxEntryJpaEntity
    OutboxRelay
    OutboxWriter
  processed/          # Idempotent event processing
    ProcessedEventJpaEntity
    JpaProcessedEventRepository
```

*Each feature package contains JPA entities, mappers, and repository adapters for that domain. The `outbox/` and `processed/` packages are cross-cutting infrastructure concerns.*

## JPA Entities

```java
@Entity
@Table(name = "companies")
public class CompanyJpaEntity {
    @Id
    private UUID id;
    private String name;
    private String registrationNumber;
    @Enumerated(EnumType.STRING)
    private CompanyStatus status;
    private Instant onboardedAt;
    @Version
    private long version;
}

@Entity
@Table(name = "customers")
public class CustomerJpaEntity {
    @Id
    private UUID id;
    private UUID companyId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String countryCode;
    @Enumerated(EnumType.STRING)
    private KycTier kycTier;
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;
    @Version
    private long version;
}

@Entity
@Table(name = "kyc_cases")
public class KycCaseJpaEntity {
    @Id
    private UUID id;
    private UUID customerId;
    @Enumerated(EnumType.STRING)
    private KycTier targetTier;
    private String checkResultsJson;
    @Enumerated(EnumType.STRING)
    private KycStatus status;
    @Version
    private long version;
}

@Entity
@Table(name = "kyc_evaluation_sagas")
public class KycEvaluationSagaJpaEntity {
    @Id
    private UUID id;
    private UUID customerId;
    private String requiredChecksJson;
    private String completedChecksJson;
    @Enumerated(EnumType.STRING)
    private KycEvaluationSagaState state;
    @Version
    private long version;
}

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {
    @Id
    private UUID id;
    private String accountNumber;
    private UUID ownerId;
    @Enumerated(EnumType.STRING)
    private AccountType type;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    @Version
    private long version;
}

@Entity
@Table(name = "journal_entries")
public class JournalEntryJpaEntity {
    @Id
    private UUID id;
    private Instant postedAt;
    private String reference;
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.PERSIST, orphanRemoval = false)
    private List<LedgerLineJpaEntity> lines;
}

@Entity
@Table(name = "ledger_lines")
public class LedgerLineJpaEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id")
    private JournalEntryJpaEntity journalEntry;
    private UUID accountId;
    private BigDecimal amount;
    private String currency;
    @Enumerated(EnumType.STRING)
    private EntryDirection direction;
}

@Entity
@Table(name = "transfers")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transfer_kind")
public abstract class TransferJpaEntity {
    @Id
    private UUID id;
    private BigDecimal amount;
    private String currency;
    @Enumerated(EnumType.STRING)
    private TransferStatus status;
    @Version
    private long version;
}

@Entity
@DiscriminatorValue("INTERNAL")
public final class InternalTransferJpaEntity extends TransferJpaEntity {
    private UUID sourceAccountId;
    private UUID destinationAccountId;
}

@Entity
@DiscriminatorValue("INBOUND_EXTERNAL")
public final class InboundExternalTransferJpaEntity extends TransferJpaEntity {
    private UUID destinationAccountId;
    private String originatingPartyReference;
}

@Entity
@DiscriminatorValue("OUTBOUND_EXTERNAL")
public final class OutboundExternalTransferJpaEntity extends TransferJpaEntity {
    private UUID sourceAccountId;
    private String beneficiaryPartyReference;
}

@Entity
@Table(name = "transfer_sagas")
public class TransferSagaJpaEntity {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private TransferSagaState state;
    private UUID accountId;
    private UUID entryId;
    @Version
    private long version;
}

@Entity
@Table(name = "cards")
public class CardJpaEntity {
    @Id
    private UUID id;
    private String cardToken;
    private UUID linkedAccountId;
    @Enumerated(EnumType.STRING)
    private CardStatus status;
    private int expiryYear;
    private int expiryMonth;
    @Version
    private long version;
}

@Entity
@Table(name = "disputes")
public class DisputeJpaEntity {
    @Id
    private UUID id;
    private UUID cardId;
    private UUID originatingTransferId;
    private String reasonCode;
    @Enumerated(EnumType.STRING)
    private DisputeStatus status;
    @Version
    private long version;
}

@Entity
@Table(name = "chargeback_sagas")
public class ChargebackSagaJpaEntity {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private ChargebackSagaState state;
    @Version
    private long version;
}

@Entity
@Table(name = "limit_policies")
public class LimitPolicyJpaEntity {
    @Id
    private UUID id;
    @Enumerated(EnumType.STRING)
    private KycTier applicableTier;
    private BigDecimal maxSingleTransactionAmount;
    private String maxSingleTransactionCurrency;
    private BigDecimal maxRollingWindowAmount;
    private String maxRollingWindowCurrency;
    private long rollingWindowDurationSeconds;
}

@Entity
@Table(name = "api_keys")
public class ApiKeyJpaEntity {
    @Id
    private UUID id;
    private UUID ownerCompanyId;
    private String hashedSecret;
    private String scopesJson;
    @Enumerated(EnumType.STRING)
    private ApiKeyStatus status;
    @Version
    private long version;
}

@Entity
@Table(name = "webhook_subscriptions")
public class WebhookSubscriptionJpaEntity {
    @Id
    private UUID id;
    private UUID ownerCompanyId;
    private String callbackUrl;
    private String eventTypesJson;
    @Enumerated(EnumType.STRING)
    private WebhookSubscriptionStatus status;
    @Version
    private long version;
}

@Entity
@Table(name = "webhook_deliveries")
public class WebhookDeliveryJpaEntity {
    @Id
    private UUID id;
    private UUID subscriptionId;
    private String payload;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    private int attemptCount;
    @Version
    private long version;
}
```
*Persistence stays outside `atlaspay-core`: JPA annotations live only on adapter-side classes, while value objects are flattened into primitive columns (or JSON text for small collections) so the domain model remains persistence-ignorant. Only mutable aggregates carry `@Version`; `JournalEntry` and `LimitPolicy` do not because they are append-only/immutable by design.*

*The `Transfer` sealed hierarchy uses JPA single-table inheritance with a discriminator. For a closed subtype set with shared identity, amount, and status fields, single-table avoids polymorphic join trees on every repository load while still preserving explicit subtype reconstruction in `TransferMapper`; that matches common JPA guidance for small, stable hierarchies where read-path simplicity matters more than sparse nullable columns.*

## Mapper Classes

```java
public final class CompanyMapper {
    public CompanyJpaEntity toJpaEntity(Company domain);
    public Company toDomain(CompanyJpaEntity entity);
}

public final class CustomerMapper {
    public CustomerJpaEntity toJpaEntity(Customer domain);
    public Customer toDomain(CustomerJpaEntity entity);
}

public final class KycCaseMapper {
    public KycCaseJpaEntity toJpaEntity(KycCase domain);
    public KycCase toDomain(KycCaseJpaEntity entity);
}

public final class KycEvaluationSagaMapper {
    public KycEvaluationSagaJpaEntity toJpaEntity(KycEvaluationSaga domain);
    public KycEvaluationSaga toDomain(KycEvaluationSagaJpaEntity entity);
}

public final class AccountMapper {
    public AccountJpaEntity toJpaEntity(Account domain);
    public Account toDomain(AccountJpaEntity entity);
}

public final class JournalEntryMapper {
    public JournalEntryJpaEntity toJpaEntity(JournalEntry domain);
    public JournalEntry toDomain(JournalEntryJpaEntity entity);
}

public final class TransferMapper {
    public TransferJpaEntity toJpaEntity(Transfer domain);
    public Transfer toDomain(TransferJpaEntity entity);
}

public final class TransferSagaMapper {
    public TransferSagaJpaEntity toJpaEntity(TransferSaga domain);
    public TransferSaga toDomain(TransferSagaJpaEntity entity);
}

public final class CardMapper {
    public CardJpaEntity toJpaEntity(Card domain);
    public Card toDomain(CardJpaEntity entity);
}

public final class DisputeMapper {
    public DisputeJpaEntity toJpaEntity(Dispute domain);
    public Dispute toDomain(DisputeJpaEntity entity);
}

public final class ChargebackSagaMapper {
    public ChargebackSagaJpaEntity toJpaEntity(ChargebackSaga domain);
    public ChargebackSaga toDomain(ChargebackSagaJpaEntity entity);
}

public final class LimitPolicyMapper {
    public LimitPolicyJpaEntity toJpaEntity(LimitPolicy domain);
    public LimitPolicy toDomain(LimitPolicyJpaEntity entity);
}

public final class ApiKeyMapper {
    public ApiKeyJpaEntity toJpaEntity(ApiKey domain);
    public ApiKey toDomain(ApiKeyJpaEntity entity);
}

public final class WebhookSubscriptionMapper {
    public WebhookSubscriptionJpaEntity toJpaEntity(WebhookSubscription domain);
    public WebhookSubscription toDomain(WebhookSubscriptionJpaEntity entity);
}

public final class WebhookDeliveryMapper {
    public WebhookDeliveryJpaEntity toJpaEntity(WebhookDelivery domain);
    public WebhookDelivery toDomain(WebhookDeliveryJpaEntity entity);
}
```
*Mapping is bidirectional and adapter-local because Hexagonal Architecture keeps serialization/persistence concerns out of the domain. `toJpaEntity(...)` flattens value objects for storage; `toDomain(...)` rehydrates value objects and the aggregate root itself.*

*Because aggregates inherit private `id`/`version` state from `AggregateRoot<ID>` and expose no public setters, each core aggregate needs a package-private/protected reconstruction path used only by infrastructure (for example, a package-private rehydration constructor or static `rehydrate(...)` factory) that restores prior state without replaying domain events, then calls the inherited `version(long)` hook.*

## Spring Data Repository Contracts

```java
public interface SpringDataCompanyRepository extends JpaRepository<CompanyJpaEntity, UUID> {
}

public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {
}

public interface SpringDataKycCaseRepository extends JpaRepository<KycCaseJpaEntity, UUID> {
}

public interface SpringDataKycEvaluationSagaRepository extends JpaRepository<KycEvaluationSagaJpaEntity, UUID> {
}

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
}

public interface SpringDataJournalEntryRepository extends JpaRepository<JournalEntryJpaEntity, UUID> {
}

public interface SpringDataTransferRepository extends JpaRepository<TransferJpaEntity, UUID> {
}

public interface SpringDataTransferSagaRepository extends JpaRepository<TransferSagaJpaEntity, UUID> {
}

public interface SpringDataCardRepository extends JpaRepository<CardJpaEntity, UUID> {
}

public interface SpringDataDisputeRepository extends JpaRepository<DisputeJpaEntity, UUID> {
}

public interface SpringDataChargebackSagaRepository extends JpaRepository<ChargebackSagaJpaEntity, UUID> {
}

public interface SpringDataLimitPolicyRepository extends JpaRepository<LimitPolicyJpaEntity, UUID> {
}

public interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyJpaEntity, UUID> {
    Optional<ApiKeyJpaEntity> findByHashedSecret(String hashedSecret);
}

public interface SpringDataWebhookSubscriptionRepository extends JpaRepository<WebhookSubscriptionJpaEntity, UUID> {
    List<WebhookSubscriptionJpaEntity> findByStatusAndEventTypesJsonContaining(WebhookSubscriptionStatus status, String eventType);
}

public interface SpringDataWebhookDeliveryRepository extends JpaRepository<WebhookDeliveryJpaEntity, UUID> {
    List<WebhookDeliveryJpaEntity> findByStatus(DeliveryStatus status);
    List<WebhookDeliveryJpaEntity> findBySubscriptionId(UUID subscriptionId);
}
```
*These interfaces are the thin Spring Data contracts hidden behind the adapter classes. Their job is only storage-centric query declaration; all value-object conversion and domain exception translation still belongs in the `Jpa*Repository` adapters above them.*

## Repository Adapter Implementations

```java
public final class JpaCompanyRepository implements CompanyRepository {
    private final SpringDataCompanyRepository jpaRepository;
    private final CompanyMapper mapper;

    @Override
    public Optional<Company> findById(CompanyId id);

    @Override
    public Company save(Company aggregate);

    @Override
    public Page<Company> findAll(int pageNumber, int pageSize);
}

public final class JpaCustomerRepository implements CustomerRepository {
    private final SpringDataCustomerRepository jpaRepository;
    private final CustomerMapper mapper;

    @Override
    public Optional<Customer> findById(CustomerId id);

    @Override
    public Customer save(Customer aggregate);

    @Override
    public Page<Customer> findAll(int pageNumber, int pageSize);
}

public final class JpaKycCaseRepository implements KycCaseRepository {
    private final SpringDataKycCaseRepository jpaRepository;
    private final KycCaseMapper mapper;

    @Override
    public Optional<KycCase> findById(KycCaseId id);

    @Override
    public KycCase save(KycCase aggregate);

    @Override
    public Page<KycCase> findAll(int pageNumber, int pageSize);
}

public final class JpaKycEvaluationSagaRepository implements KycEvaluationSagaRepository {
    private final SpringDataKycEvaluationSagaRepository jpaRepository;
    private final KycEvaluationSagaMapper mapper;

    @Override
    public Optional<KycEvaluationSaga> findById(KycCaseId id);

    @Override
    public KycEvaluationSaga save(KycEvaluationSaga aggregate);

    @Override
    public Page<KycEvaluationSaga> findAll(int pageNumber, int pageSize);
}

public final class JpaAccountRepository implements AccountRepository {
    private final SpringDataAccountRepository jpaRepository;
    private final AccountMapper mapper;

    @Override
    public Optional<Account> findById(AccountId id);

    @Override
    public Account save(Account aggregate);

    @Override
    public Page<Account> findAll(int pageNumber, int pageSize);
}

public final class JpaJournalEntryRepository implements JournalEntryRepository {
    private final SpringDataJournalEntryRepository jpaRepository;
    private final JournalEntryMapper mapper;

    @Override
    public Optional<JournalEntry> findById(JournalEntryId id);

    @Override
    public JournalEntry save(JournalEntry aggregate);

    @Override
    public Page<JournalEntry> findAll(int pageNumber, int pageSize);
}

public final class JpaTransferRepository implements TransferRepository {
    private final SpringDataTransferRepository jpaRepository;
    private final TransferMapper mapper;

    @Override
    public Optional<Transfer> findById(TransferId id);

    @Override
    public Transfer save(Transfer aggregate);

    @Override
    public Page<Transfer> findAll(int pageNumber, int pageSize);
}

public final class JpaTransferSagaRepository implements TransferSagaRepository {
    private final SpringDataTransferSagaRepository jpaRepository;
    private final TransferSagaMapper mapper;

    @Override
    public Optional<TransferSaga> findById(TransferId id);

    @Override
    public TransferSaga save(TransferSaga aggregate);

    @Override
    public Page<TransferSaga> findAll(int pageNumber, int pageSize);
}

public final class JpaCardRepository implements CardRepository {
    private final SpringDataCardRepository jpaRepository;
    private final CardMapper mapper;

    @Override
    public Optional<Card> findById(CardId id);

    @Override
    public Card save(Card aggregate);

    @Override
    public Page<Card> findAll(int pageNumber, int pageSize);
}

public final class JpaDisputeRepository implements DisputeRepository {
    private final SpringDataDisputeRepository jpaRepository;
    private final DisputeMapper mapper;

    @Override
    public Optional<Dispute> findById(DisputeId id);

    @Override
    public Dispute save(Dispute aggregate);

    @Override
    public Page<Dispute> findAll(int pageNumber, int pageSize);
}

public final class JpaChargebackSagaRepository implements ChargebackSagaRepository {
    private final SpringDataChargebackSagaRepository jpaRepository;
    private final ChargebackSagaMapper mapper;

    @Override
    public Optional<ChargebackSaga> findById(DisputeId id);

    @Override
    public ChargebackSaga save(ChargebackSaga aggregate);

    @Override
    public Page<ChargebackSaga> findAll(int pageNumber, int pageSize);
}

public final class JpaLimitPolicyRepository implements LimitPolicyRepository {
    private final SpringDataLimitPolicyRepository jpaRepository;
    private final LimitPolicyMapper mapper;

    @Override
    public Optional<LimitPolicy> findById(LimitPolicyId id);

    @Override
    public LimitPolicy save(LimitPolicy aggregate);

    @Override
    public Page<LimitPolicy> findAll(int pageNumber, int pageSize);
}

public final class JpaApiKeyRepository implements ApiKeyRepository {
    private final SpringDataApiKeyRepository jpaRepository;
    private final ApiKeyMapper mapper;

    @Override
    public Optional<ApiKey> findById(ApiKeyId id);

    @Override
    public ApiKey save(ApiKey aggregate);

    @Override
    public Page<ApiKey> findAll(int pageNumber, int pageSize);

    @Override
    public Optional<ApiKey> findByHashedSecret(HashedSecret hashedSecret);
}

public final class JpaWebhookSubscriptionRepository implements WebhookSubscriptionRepository {
    private final SpringDataWebhookSubscriptionRepository jpaRepository;
    private final WebhookSubscriptionMapper mapper;

    @Override
    public Optional<WebhookSubscription> findById(WebhookSubscriptionId id);

    @Override
    public WebhookSubscription save(WebhookSubscription aggregate);

    @Override
    public Page<WebhookSubscription> findAll(int pageNumber, int pageSize);

    @Override
    public List<WebhookSubscription> findActiveByEventType(String eventType);
}

public final class JpaWebhookDeliveryRepository implements WebhookDeliveryRepository {
    private final SpringDataWebhookDeliveryRepository jpaRepository;
    private final WebhookDeliveryMapper mapper;

    @Override
    public Optional<WebhookDelivery> findById(WebhookDeliveryId id);

    @Override
    public WebhookDelivery save(WebhookDelivery aggregate);

    @Override
    public Page<WebhookDelivery> findAll(int pageNumber, int pageSize);

    @Override
    public List<WebhookDelivery> findPendingRetries();

    @Override
    public List<WebhookDelivery> findBySubscriptionId(WebhookSubscriptionId id);
}
```
*All fifteen core repository interfaces get exactly one adapter. Each adapter delegates ordinary CRUD/paging to Spring Data `JpaRepository`, then layers AtlasPay-specific concerns on top: mapping, value-object reconstruction, and translation of `OptimisticLockingFailureException` into a domain-level concurrency conflict. `JpaRepository` is preferred over hand-written `EntityManager` code because AtlasPay's repositories are shape-stable CRUD ports; the custom behavior lives in adapters, not in bespoke persistence plumbing.*

## Outbox

```java
public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}

@Entity
@Table(name = "outbox_entries")
public class OutboxEntry {
    @Id
    private UUID id;
    private String aggregateType;
    private UUID aggregateId;
    private String eventType;
    private String payload;
    private Instant occurredOn;
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
    @Version
    private long version;
}

public final class OutboxWriter {
    public void saveAggregateWithEvents(AggregateRoot<?> aggregate);
}

public final class OutboxRelay {
    public void pollAndPublish();
    public void retryFailed();
}
```
*`OutboxWriter.saveAggregateWithEvents(...)` is the missing transactional seam from `docs/DESIGN.md`: within one database transaction it persists the mapped aggregate row, drains `aggregate.pullEvents()`, serializes each event into an `OutboxEntry`, and inserts those rows before commit. That is EIP **Guaranteed Delivery** without a database/Kafka XA transaction.*

*`OutboxRelay` is deliberately asynchronous and infrastructure-owned: poll pending rows, publish them, then mark them `PUBLISHED`. Because multiple relay instances may race, the outbox row itself carries `@Version` so status changes remain optimistic-lock-safe.*

## Flyway Migrations

```text
V001__create_companies_table.sql
V002__create_customers_table.sql
V003__create_kyc_cases_table.sql
V004__create_kyc_evaluation_sagas_table.sql
V005__create_accounts_table.sql
V006__create_journal_entries_table.sql
V007__create_ledger_lines_table.sql
V008__create_transfers_table.sql
V009__create_transfer_sagas_table.sql
V010__create_cards_table.sql
V011__create_disputes_table.sql
V012__create_chargeback_sagas_table.sql
V013__create_limit_policies_table.sql
V014__create_api_keys_table.sql
V015__create_webhook_subscriptions_table.sql
V016__create_webhook_deliveries_table.sql
V017__create_processed_events_table.sql
V018__create_outbox_entries_table.sql
```
*Foreign-key order matters: `customers -> companies`; `kyc_cases -> customers`; `accounts -> customers`; `ledger_lines -> journal_entries` and reference `accounts`; `transfers` reference participating accounts; `disputes -> cards/transfers`; `api_keys` and `webhook_subscriptions -> companies`; `webhook_deliveries -> webhook_subscriptions`. `processed_events` and `outbox_entries` are late because they depend on no business tables but are consumed by messaging/publishing infrastructure.*
