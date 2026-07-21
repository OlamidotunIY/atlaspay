package core.cards.valueobjects;

public record AuthorizationDecision(boolean approved, String reason) {

    public AuthorizationDecision {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null or blank");
        }
    }
}
