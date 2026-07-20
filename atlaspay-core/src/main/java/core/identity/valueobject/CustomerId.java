package core.identity.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CustomerId(UUID value) {
    public CustomerId {
        Objects.requireNonNull(value, "CustomerId value cannot be null");
    }

    public static CustomerId newId() {
        return new CustomerId(UUID.randomUUID());
    }

    public static CustomerId fromString(String id) {
        return new CustomerId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
