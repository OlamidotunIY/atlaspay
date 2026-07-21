package core.transfers.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record TransferId(UUID value) {

    public TransferId {
        Objects.requireNonNull(value, "TransferId value cannot be null");
    }

    public static TransferId newId() {
        return new TransferId(UUID.randomUUID());
    }

    public static TransferId fromString(String id) {
        return new TransferId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
