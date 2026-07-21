package core.cards.valueobjects;

import core.ledger.valueobjects.Money;

public record DisputeResolution(DisputeStatus outcome, Money adjustedAmount, String notes) {

    public DisputeResolution {
        if (outcome == null) {
            throw new IllegalArgumentException("Dispute outcome cannot be null");
        }
        if (adjustedAmount == null) {
            throw new IllegalArgumentException("Adjusted amount cannot be null");
        }
    }
}
