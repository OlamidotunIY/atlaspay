package core.access.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record WebhookDeliveryId(UUID value) {

    public WebhookDeliveryId {
        Objects.requireNonNull(value, "WebhookDeliveryId value cannot be null");
    }

    public static WebhookDeliveryId newId() {
        return new WebhookDeliveryId(UUID.randomUUID());
    }

    public static WebhookDeliveryId fromString(String id) {
        return new WebhookDeliveryId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
