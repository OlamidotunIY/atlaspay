package core.access.repository;

import core.access.valueobjects.RetryDecision;

public interface WebhookRetryPolicy {
    RetryDecision decideNextStep(int attemptCount); // single authoritative RETRY-vs-DEAD_LETTER decision (with backoff duration when retrying)
}
