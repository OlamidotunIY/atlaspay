package core.access.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record WebhookSubscriptionId(UUID value) {

    public WebhookSubscriptionId {
        Objects.requireNonNull(value, "WebhookSubscriptionId value cannot be null");
    }

    public static WebhookSubscriptionId newId() {
        return new WebhookSubscriptionId(UUID.randomUUID());
    }

    public static WebhookSubscriptionId fromString(String id) {
        return new WebhookSubscriptionId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
