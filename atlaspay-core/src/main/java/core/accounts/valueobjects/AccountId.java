package core.accounts.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "WebhookSubscriptionId value cannot be null");
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId fromString(String id) {
        return new AccountId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
