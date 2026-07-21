package core.ledger.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record JournalEntryId(UUID value) {

    public JournalEntryId {
        Objects.requireNonNull(value, "JournalEntryId value cannot be null");
    }

    public static JournalEntryId newId() {
        return new JournalEntryId(UUID.randomUUID());
    }

    public static JournalEntryId fromString(String id) {
        return new JournalEntryId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
