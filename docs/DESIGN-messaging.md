# AtlasPay — Messaging Design Reference (atlaspay-messaging)

> Infrastructure adapter for Kafka-based event publishing and consumption. Complements docs/DESIGN.md (core domain model).

## Topic-to-Event Mapping

### `atlaspay.identity.events`
- `CompanyRegistered`
- `CompanyVerified`
- `CompanySuspended`
- `CustomerOnboarded`
- `CustomerKycTierChanged`
- `CustomerKycDecided`
- `KycCaseOpened`
- `KycCheckResultRecorded`

### `atlaspay.accounts.events`
- `AccountOpened`
- `AccountFrozen`
- `AccountClosed`

### `atlaspay.ledger.events`
- `JournalEntryPosted`

### `atlaspay.transfers.events`
- `TransferInitiated`
- `TransferPosted`
- `TransferFailed`
- `TransferReversed`

### `atlaspay.cards.events`
- `CardIssued`
- `CardActivated`
- `CardBlocked`
- `DisputeFiled`
- `DisputeResolved`

### `atlaspay.limits.events`
- `LimitPolicyCreated`
- `LimitBreached`

### `atlaspay.access.events`
- `ApiKeyIssued`
- `ApiKeyRevoked`
- `WebhookSubscriptionCreated`
- `WebhookSubscriptionPaused`
- `WebhookSubscriptionResumed`
- `WebhookSubscriptionDisabled`
- `WebhookDeliveryScheduled`
- `WebhookDeliveryRetryScheduled`
- `WebhookDeliveryDelivered`
- `WebhookDeliveryDeadLettered`

## Producer Side

```java
public final class KafkaEventPublisher {
    public void publish(String topic, DomainEvent event);
}
```
*Kafka records use the aggregate ID as the message key. That keeps all events for one aggregate on the same partition, which is the practical way to preserve per-aggregate ordering without requiring global ordering across the whole topic.*

```java
public final class OutboxRelay {
    private final KafkaEventPublisher publisher;

    public void pollAndPublish();
}
```
*`OutboxRelay` is the bridge from relational durability to Kafka: load `PENDING` rows, deserialize `payload`, resolve the topic from `eventType`, publish with key = aggregate ID, then mark the row `PUBLISHED`. Failures leave the row for retry, giving at-least-once publication.*

## Consumer Side

```java
public final class IdentityEventsConsumer {
    public void onMessage(ConsumerRecord<String, String> record);
    private boolean alreadyProcessed(UUID eventId);
}

public final class AccountsEventsConsumer {
    public void onMessage(ConsumerRecord<String, String> record);
    private boolean alreadyProcessed(UUID eventId);
}

public final class LedgerEventsConsumer {
    public void onMessage(ConsumerRecord<String, String> record);
    private boolean alreadyProcessed(UUID eventId);
}

public final class TransfersEventsConsumer {
    public void onMessage(ConsumerRecord<String, String> record);
    private boolean alreadyProcessed(UUID eventId);
}

public final class CardsEventsConsumer {
    public void onMessage(ConsumerRecord<String, String> record);
    private boolean alreadyProcessed(UUID eventId);
}

public final class LimitsEventsConsumer {
    public void onMessage(ConsumerRecord<String, String> record);
    private boolean alreadyProcessed(UUID eventId);
}

public final class AccessEventsConsumer {
    public void onMessage(ConsumerRecord<String, String> record);
    private boolean alreadyProcessed(UUID eventId);
}
```
*Every consumer is an EIP **Idempotent Receiver**. Kafka delivery is at-least-once, so each handler must check/store event IDs before applying side effects or loading/saving sagas.*

## Processed-Event Store

```java
@Entity
@Table(name = "processed_events")
public class ProcessedEventJpaEntity {
    @Id
    private UUID eventId;
    private Instant processedAt;
}

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventJpaEntity, UUID> {
}
```
*The processed-event table physically lives in `atlaspay-persistence`, but messaging consumers depend on it for idempotency. That is a normal Hexagonal cross-module relationship: storage mechanics remain in persistence; duplicate-suppression policy belongs to the messaging adapter.*

## Saga Event Delivery

```java
public sealed interface TransferEvent extends DomainEvent permits TransferInitiated, TransferPosted, TransferFailed, TransferReversed {
}

public sealed interface KycEvent extends DomainEvent permits KycCaseOpened, KycCheckResultRecorded {
}

public sealed interface ChargebackEvent extends DomainEvent permits DisputeFiled, DisputeResolved {
}

public final class SagaEventRouter {
    public void routeToTransferSaga(TransferEvent event);
    public void routeToKycEvaluationSaga(KycEvent event);
    public void routeToChargebackSaga(ChargebackEvent event);
}
```
*Routing is correlation-by-aggregate ID: `TransferSaga` loads by `transferId`, `KycEvaluationSaga` by `kycCaseId`, and `ChargebackSaga` by `disputeId`. Each consumer deserializes the event, looks up the matching saga instance, invokes the relevant `on...` transition method, and saves the saga back through its repository adapter.*

## Dead Letter Handling

```java
public final class DeadLetterHandler {
    public void handle(String sourceTopic, ConsumerRecord<String, String> record, Exception failure);
    public String deadLetterTopicFor(String sourceTopic);
}
```
*This dead-lettering is Kafka-internal infrastructure, not the Platform Access domain's `WebhookDeliveryDeadLettered` concept. Internal consumer failures are rerouted to topic-specific DLQs such as `atlaspay.identity.events.dlq` after three delivery attempts; webhook dead-lettering remains a business event emitted by the `WebhookDelivery` aggregate.*
