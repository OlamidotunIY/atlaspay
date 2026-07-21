package core.access;

import core.access.valueobjects.RetryDecision;

public interface WebhookRetryPolicy {
    RetryDecision decideNextStep(int attemptCount); // single authoritative RETRY-vs-DEAD_LETTER decision (with backoff duration when retrying)
}
