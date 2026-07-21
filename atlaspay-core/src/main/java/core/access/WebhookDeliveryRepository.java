package core.access;

import core.access.valueobjects.WebhookDeliveryId;
import core.access.valueobjects.WebhookSubscriptionId;
import core.shared.Repository;

import java.util.List;

public interface WebhookDeliveryRepository extends Repository<WebhookDelivery, WebhookDeliveryId> {
    List<WebhookDelivery> findPendingRetries();                       // deliveries due for retry
    List<WebhookDelivery> findBySubscriptionId(WebhookSubscriptionId id); // delivery history/audit
}
