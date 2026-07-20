package core.identity.valueobject;

public record KycDecision(KycStatus outcome, String reviewer, String notes) {
    public KycDecision {
        if (outcome == null) {
            throw new IllegalArgumentException("KYC outcome cannot be null");
        }
        if (reviewer == null || reviewer.isBlank()) {
            throw new IllegalArgumentException("Reviewer cannot be null or blank");
        }
    }
}
