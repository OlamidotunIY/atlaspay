package core.access;

import core.access.valueobjects.WebhookSubscriptionId;
import core.shared.Repository;

import java.util.List;

public interface WebhookSubscriptionRepository extends Repository<WebhookSubscription, WebhookSubscriptionId> {
    List<WebhookSubscription> findActiveByEventType(String eventType); // fan-out targets for a published event
}
