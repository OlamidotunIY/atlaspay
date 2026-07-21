package core.ledger.valueobjects;

import java.util.Objects;

public record TransactionReference(String value) {

    public TransactionReference {
        Objects.requireNonNull(value, "TransactionReference value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TransactionReference must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
