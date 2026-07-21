package core.access;

import core.access.events.WebhookSubscriptionCreated;
import core.access.events.WebhookSubscriptionDisabled;
import core.access.events.WebhookSubscriptionPaused;
import core.access.events.WebhookSubscriptionResumed;
import core.access.valueobjects.WebhookSubscriptionId;
import core.access.valueobjects.WebhookSubscriptionStatus;
import core.identity.valueobject.CompanyId;
import core.shared.AggregateRoot;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class WebhookSubscription extends AggregateRoot<WebhookSubscriptionId> {
    private final CompanyId ownerCompanyId;
    private final URI callbackUrl;
    private final Set<String> eventTypes;
    private WebhookSubscriptionStatus status;

    public WebhookSubscription(WebhookSubscriptionId id, CompanyId ownerCompanyId, URI callbackUrl, Set<String> eventTypes) {
        super(id);
        this.ownerCompanyId = ownerCompanyId;
        this.callbackUrl = callbackUrl;
        this.eventTypes = eventTypes;
        register(new WebhookSubscriptionCreated(UUID.randomUUID(), Instant.now(), id, ownerCompanyId));
    }

    public void pause() {
        if (!this.status.equals(WebhookSubscriptionStatus.ACTIVE)) {
            throw new IllegalStateException("Cannot pause a subscription that is not active.");
        }
        this.status = WebhookSubscriptionStatus.PAUSED;
        register(new WebhookSubscriptionPaused(UUID.randomUUID(), Instant.now(), id()));
    }

    public void resume() {
        if (!this.status.equals(WebhookSubscriptionStatus.PAUSED)) {
            throw new IllegalStateException("Cannot resume a subscription that is not paused.");
        }
        this.status = WebhookSubscriptionStatus.ACTIVE;
        register(new WebhookSubscriptionResumed(UUID.randomUUID(), Instant.now(), id()));
    }

    public void disable(String reason) {
        if (!this.status.equals(WebhookSubscriptionStatus.ACTIVE) && !this.status.equals(WebhookSubscriptionStatus.PAUSED)) {
            throw new IllegalStateException("Cannot disable a subscription that is not active or paused.");
        }
        this.status = WebhookSubscriptionStatus.DISABLED;
        register(new WebhookSubscriptionDisabled(UUID.randomUUID(), Instant.now(), id(), reason));
    }
}
