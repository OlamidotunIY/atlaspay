package core.access;

import core.access.events.WebhookDeliveryDeadLettered;
import core.access.events.WebhookDeliveryDelivered;
import core.access.events.WebhookDeliveryRetryScheduled;
import core.access.events.WebhookDeliveryScheduled;
import core.access.valueobjects.DeliveryStatus;
import core.access.valueobjects.RetryDecision;
import core.access.valueobjects.WebhookDeliveryId;
import core.access.valueobjects.WebhookSubscriptionId;
import core.shared.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class WebhookDelivery extends AggregateRoot<WebhookDeliveryId> {
    private final WebhookSubscriptionId subscriptionId;
    private final String payload;
    private DeliveryStatus status;
    private int attemptCount;

    public WebhookDelivery(WebhookDeliveryId id, WebhookSubscriptionId subscriptionId, String payload) {
        super(id);
        this.subscriptionId = subscriptionId;
        this.payload = payload;
        this.status = DeliveryStatus.PENDING;
        this.attemptCount = 0;
        register(new WebhookDeliveryScheduled(UUID.randomUUID(), Instant.now(), id, subscriptionId));
    }

    public int attemptCount() {
        return attemptCount;
    }

    public void recordAttemptFailure(RetryDecision decision) {
        Objects.requireNonNull(decision, "decision cannot be null");

        if (status == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Cannot record failure for a delivered webhook");
        }
        if (status == DeliveryStatus.DEAD_LETTERED) {
            throw new IllegalStateException("Cannot record failure for a dead-lettered webhook");
        }

        attemptCount++;

        if (decision instanceof RetryDecision.Retry) {
            status = DeliveryStatus.RETRYING;
            register(new WebhookDeliveryRetryScheduled(UUID.randomUUID(), Instant.now(), id(), attemptCount));
        } else if (decision instanceof RetryDecision.DeadLetter) {
            status = DeliveryStatus.DEAD_LETTERED;
            register(new WebhookDeliveryDeadLettered(UUID.randomUUID(), Instant.now(), id()));
        } else {
            throw new IllegalArgumentException("Unknown RetryDecision type: " + decision.getClass().getName());
        }
    }

    public void recordDelivered() {
        if (status == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Webhook is already marked as delivered");
        }
        if (status == DeliveryStatus.DEAD_LETTERED) {
            throw new IllegalStateException("Cannot mark a dead-lettered webhook as delivered");
        }
        status = DeliveryStatus.DELIVERED;
        register(new WebhookDeliveryDelivered(UUID.randomUUID(), Instant.now(), id()));
    }
}
