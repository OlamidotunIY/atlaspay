package core.identity.valueobject;

import java.util.Objects;
import java.util.UUID;

public record KycCaseId(UUID value) {
    public KycCaseId {
        Objects.requireNonNull(value, "KycCaseId value cannot be null");
    }

    public static KycCaseId newId() {
        return new KycCaseId(UUID.randomUUID());
    }

    public static KycCaseId fromString(String id) {
        return new KycCaseId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
