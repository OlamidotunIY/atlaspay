package core.cards.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record DisputeId(UUID value) {

    public DisputeId {
        Objects.requireNonNull(value, "DisputeId value cannot be null");
    }

    public static DisputeId newId() {
        return new DisputeId(UUID.randomUUID());
    }

    public static DisputeId fromString(String id) {
        return new DisputeId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
