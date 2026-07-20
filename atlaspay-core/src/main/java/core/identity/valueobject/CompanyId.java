package core.identity.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CompanyId(UUID value) {
    public CompanyId {
        Objects.requireNonNull(value, "CompanyId value cannot be null");
    }

    public static CompanyId newId() {
        return new CompanyId(UUID.randomUUID());
    }

    public static CompanyId fromString(String id) {
        return new CompanyId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
