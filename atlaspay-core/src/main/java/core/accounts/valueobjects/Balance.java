package core.accounts.valueobjects;

import core.ledger.valueobjects.Money;

import java.time.Instant;

public record Balance(Money amount, Instant asOf) {
    public Balance {
        java.util.Objects.requireNonNull(amount, "amount must not be null");
        java.util.Objects.requireNonNull(asOf, "asOf must not be null");
    }
}
