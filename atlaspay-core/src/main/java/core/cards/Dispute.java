package core.cards;

import core.cards.events.DisputeFiled;
import core.cards.events.DisputeResolved;
import core.cards.valueobjects.*;
import core.shared.AggregateRoot;
import core.transfers.valueobjects.TransferId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Dispute extends AggregateRoot<DisputeId> {
    private final CardId cardId;
    private final TransferId originatingTransferId;
    private ReasonCode reasonCode;
    private DisputeStatus status;                 // FILED, UNDER_REVIEW, RESOLVED_MERCHANT, RESOLVED_CUSTOMER

    public Dispute(DisputeId id, CardId cardId, TransferId originatingTransferId, ReasonCode reasonCode) {
        super(id);
        this.cardId = cardId;
        this.originatingTransferId = originatingTransferId;
        this.reasonCode = reasonCode;
        this.status = DisputeStatus.UNDER_REVIEW;
        register(new DisputeFiled(UUID.randomUUID(), Instant.now(), id(), reasonCode));
    }

    public void resolve(DisputeResolution resolution) {
        Objects.requireNonNull(resolution, "Resolution cannot be null");
        if (status != DisputeStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Dispute can only be resolved if it is UNDER_REVIEW.");
        }
        this.status = DisputeStatus.valueOf(resolution.outcome().name());
        register(new DisputeResolved(UUID.randomUUID(), Instant.now(), id(), resolution.outcome()));
    }

    public DisputeStatus status() {
        return status;
    }
}
