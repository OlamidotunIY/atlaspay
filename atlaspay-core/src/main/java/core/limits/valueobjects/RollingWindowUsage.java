package core.limits.valueobjects;

import core.ledger.valueobjects.Money;

import java.time.Instant;

public record RollingWindowUsage(Money totalSpent, Instant windowStart, Instant windowEnd) {
    public RollingWindowUsage {
        if (totalSpent == null) {
            throw new IllegalArgumentException("totalSpent cannot be null");
        }
        if (windowStart == null) {
            throw new IllegalArgumentException("windowStart cannot be null");
        }
        if (windowEnd == null) {
            throw new IllegalArgumentException("windowEnd cannot be null");
        }
        if (windowStart.isAfter(windowEnd)) {
            throw new IllegalArgumentException("windowStart must be before windowEnd");
        }
    }
}
