package core.access;

import core.access.valueobjects.WebhookSubscriptionId;
import core.access.valueobjects.WebhookSubscriptionStatus;
import core.identity.valueobject.CompanyId;
import core.shared.AggregateRoot;

import java.net.URI;
import java.util.Set;

public final class WebhookSubscription extends AggregateRoot<WebhookSubscriptionId> {
    private final CompanyId ownerCompanyId;
    private final URI callbackUrl;
    private final Set<String> eventTypes;             // subscribed event type names
    private WebhookSubscriptionStatus status;             // ACTIVE, PAUSED, DISABLED

    public WebhookSubscription(WebhookSubscriptionId webhookSubscriptionId, CompanyId ownerCompanyId, URI callbackUrl, Set<String> eventTypes) {
        super(webhookSubscriptionId);
        this.ownerCompanyId = ownerCompanyId;
        this.callbackUrl = callbackUrl;
        this.eventTypes = eventTypes;
    }

    public void pause(){}
    public void resume(){}                        // PAUSED -> ACTIVE; company-initiated; raises WebhookSubscriptionResumed
    public void disable(String reason){}      // -> DISABLED after repeated delivery failures; system-initiated, not reachable via company self-service; raises WebhookSubscriptionDisabled
}
