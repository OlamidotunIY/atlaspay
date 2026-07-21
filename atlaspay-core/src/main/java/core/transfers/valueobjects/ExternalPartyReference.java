package core.transfers.valueobjects;

import java.util.Objects;

public record ExternalPartyReference(String partyId, String partyName) {
    public ExternalPartyReference {
        Objects.requireNonNull(partyId, "partyId cannot be null");
        Objects.requireNonNull(partyName, "partyName cannot be null");
    }
}
