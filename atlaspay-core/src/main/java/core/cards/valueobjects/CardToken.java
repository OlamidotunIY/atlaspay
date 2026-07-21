package core.cards.valueobjects;

import java.util.Objects;

public record CardToken(String value) {

    public CardToken {
        Objects.requireNonNull(value, "CardToken value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Card token must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
