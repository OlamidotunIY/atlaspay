package core.access.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record ApiKeyId(UUID value) {

    public ApiKeyId {
        Objects.requireNonNull(value, "ApiKeyId value cannot be null");
    }

    public static ApiKeyId newId() {
        return new ApiKeyId(UUID.randomUUID());
    }

    public static ApiKeyId fromString(String id) {
        return new ApiKeyId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
