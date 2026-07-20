package core.identity.valueobject;

import java.time.Instant;

public record VerificationDecision(boolean approved, String reviewer, Instant reviewedAt) {
}
