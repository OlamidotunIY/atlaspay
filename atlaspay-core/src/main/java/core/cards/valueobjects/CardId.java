package core.cards.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record CardId(UUID value) {

    public CardId {
        Objects.requireNonNull(value, "CardId value cannot be null");
    }

    public static CardId newId() {
        return new CardId(UUID.randomUUID());
    }

    public static CardId fromString(String id) {
        return new CardId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
