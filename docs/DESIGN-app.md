# AtlasPay — Application Composition Root (atlaspay-app)

> Spring Boot entrypoint, dependency injection wiring, scheduled jobs, configuration properties. Complements docs/DESIGN.md (core domain model).

## Module Structure

Single package at the root (this is the wiring/composition layer):

```
app/
  AtlasPayApplication         # @SpringBootApplication entrypoint
  RepositoryWiringConfig      # Wires core repository interfaces to JPA adapters
  DomainServicesWiringConfig  # Wires domain services to simulators/real implementations
  SecurityWiringConfig        # Wires security filters and authentication providers
  SchedulingConfig            # Configures scheduled jobs (OutboxRelay, webhook retry)
  OutboxRelayScheduler        # Scheduled polling trigger for outbox
  WebhookRetryScheduler       # Scheduled polling trigger for webhook retries
  DatabaseProperties          # @ConfigurationProperties for DB connection
  KafkaProperties             # @ConfigurationProperties for Kafka
  SimulatorProperties         # @ConfigurationProperties for simulator behavior
  WebhookProperties           # @ConfigurationProperties for webhook retry policy
  OutboxProperties            # @ConfigurationProperties for outbox polling
```

*Flat structure — this module's only job is dependency injection and scheduled job triggers. All logic lives in the modules it wires together. This is the hexagonal architecture's composition root and the only module allowed to import from every other module.*

## Main Application Class

```java
@SpringBootApplication
@EnableJpaRepositories(basePackages = "persistence")
@EnableConfigurationProperties({
        DatabaseProperties.class,
        KafkaProperties.class,
        SimulatorProperties.class,
        WebhookProperties.class,
        OutboxProperties.class
})
@EnableScheduling
@EnableKafka
@EnableTransactionManagement
public class AtlasPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(AtlasPayApplication.class, args);
    }
}
```
*`@EnableJpaRepositories` activates persistence adapters backed by Spring Data; `@EnableScheduling` turns on time-based polling/retry triggers; `@EnableKafka` activates topic listeners; `@EnableTransactionManagement` makes aggregate-save + outbox-write atomic. `atlaspay-app` is the bootstrap layer, so it is also the right place to enable configuration-properties binding across modules.*

## Dependency Injection Wiring

| Core Port (Interface) | Concrete Adapter Bean | Module |
|---|---|---|
| `CompanyRepository` | `JpaCompanyRepository` | `atlaspay-persistence` |
| `CustomerRepository` | `JpaCustomerRepository` | `atlaspay-persistence` |
| `KycCaseRepository` | `JpaKycCaseRepository` | `atlaspay-persistence` |
| `KycEvaluationSagaRepository` | `JpaKycEvaluationSagaRepository` | `atlaspay-persistence` |
| `AccountRepository` | `JpaAccountRepository` | `atlaspay-persistence` |
| `JournalEntryRepository` | `JpaJournalEntryRepository` | `atlaspay-persistence` |
| `TransferRepository` | `JpaTransferRepository` | `atlaspay-persistence` |
| `TransferSagaRepository` | `JpaTransferSagaRepository` | `atlaspay-persistence` |
| `CardRepository` | `JpaCardRepository` | `atlaspay-persistence` |
| `DisputeRepository` | `JpaDisputeRepository` | `atlaspay-persistence` |
| `ChargebackSagaRepository` | `JpaChargebackSagaRepository` | `atlaspay-persistence` |
| `LimitPolicyRepository` | `JpaLimitPolicyRepository` | `atlaspay-persistence` |
| `ApiKeyRepository` | `JpaApiKeyRepository` | `atlaspay-persistence` |
| `WebhookSubscriptionRepository` | `JpaWebhookSubscriptionRepository` | `atlaspay-persistence` |
| `WebhookDeliveryRepository` | `JpaWebhookDeliveryRepository` | `atlaspay-persistence` |
| `KycRuleEngine` | `SpecificationBasedKycRuleEngine` | `atlaspay-app` |
| `LimitEvaluationService` | `RollingWindowLimitEvaluationService` | `atlaspay-app` |
| `BankSimulator` | `InMemoryBankSimulator` | `atlaspay-simulators` |
| `CardNetworkSimulator` | `InMemoryCardNetworkSimulator` | `atlaspay-simulators` |
| `IssuingBankSimulator` | `InMemoryIssuingBankSimulator` | `atlaspay-simulators` |

*This module is the only one allowed to import from every other module. In Hexagonal terms it is the composition root: ports are declared elsewhere; concrete adapters are selected and wired here.*

## Adapter/Service Bean Configuration

```java
@Configuration
public class AtlasPayBeansConfiguration {
    @Bean
    public KycRuleEngine kycRuleEngine();

    @Bean
    public LimitEvaluationService limitEvaluationService();

    @Bean
    public BankSimulator bankSimulator(SimulatorProperties properties);

    @Bean
    public CardNetworkSimulator cardNetworkSimulator(SimulatorProperties properties);

    @Bean
    public IssuingBankSimulator issuingBankSimulator(SimulatorProperties properties);
}

public final class SpecificationBasedKycRuleEngine implements KycRuleEngine {
    @Override
    public Result<KycTier, List<String>> evaluate(Customer customer, KycCase kycCase);
}

public final class RollingWindowLimitEvaluationService implements LimitEvaluationService {
    @Override
    public Result<Void, List<String>> evaluate(AccountId accountId, KycTier tier, Money proposedAmount);
}

public final class WebhookRetryDispatcher {
    public void retry(WebhookDelivery delivery);
}
```
*These are application-owned compositions of existing core policies/ports, not new domains. They are declared here because the composition root decides which concrete strategy classes satisfy each port in a given deployment profile.*

## Scheduled Jobs

```java
@Component
public class OutboxRelayScheduler {
    private final OutboxRelay relay;

    @Scheduled(fixedDelay = 5000)
    public void pollOutbox() {
        relay.pollAndPublish();
    }
}

@Component
public class WebhookRetryScheduler {
    private final WebhookDeliveryRepository repository;
    private final WebhookRetryDispatcher dispatcher;

    @Scheduled(fixedDelay = 10000)
    public void retryPending() {
        List<WebhookDelivery> pending = repository.findPendingRetries();
        pending.forEach(dispatcher::retry);
    }
}
```
*Scheduling belongs in `atlaspay-app` because time-based triggering is wiring/configuration, not domain logic. The actual behavior still lives in the owning module: outbox publication in persistence, webhook retry decisioning in the access/messaging infrastructure around `WebhookRetryPolicy` and `WebhookDelivery`.*

## Configuration Properties

```java
@ConfigurationProperties(prefix = "atlaspay.database")
public record DatabaseProperties(String url, String username, String password) {}

@ConfigurationProperties(prefix = "atlaspay.kafka")
public record KafkaProperties(String bootstrapServers, String consumerGroupId) {}

@ConfigurationProperties(prefix = "atlaspay.simulators")
public record SimulatorProperties(int bankLatencyMs, double bankFailureRate, int cardNetworkLatencyMs) {}

@ConfigurationProperties(prefix = "atlaspay.webhooks")
public record WebhookProperties(int maxRetries, Duration initialBackoff, Duration maxBackoff) {}

@ConfigurationProperties(prefix = "atlaspay.outbox")
public record OutboxProperties(int pollBatchSize, Duration failedRetryDelay) {}
```
*Properties stay structural here: names and types only. Values vary by environment and belong in deployment-time configuration, not in design reference documents.*
