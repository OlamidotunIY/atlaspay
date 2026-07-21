package core.access;

import core.access.valueobjects.DeliveryStatus;
import core.access.valueobjects.RetryDecision;
import core.access.valueobjects.WebhookDeliveryId;
import core.access.valueobjects.WebhookSubscriptionId;
import core.shared.AggregateRoot;

public final class WebhookDelivery extends AggregateRoot<WebhookDeliveryId> {
    private final WebhookSubscriptionId subscriptionId;
    private final String payload;
    private DeliveryStatus status;
    private int attemptCount;

    public WebhookDelivery(WebhookDeliveryId webhookDeliveryId, WebhookSubscriptionId subscriptionId, String payload) {
        super(webhookDeliveryId);
        this.subscriptionId = subscriptionId;
        this.payload = payload;
    }

    public int attemptCount() {
        return attemptCount;
    }
    public void recordAttemptFailure(RetryDecision decision) {}
    public void recordDelivered() {}
}
