package core.access.valueobjects;

import java.time.Duration;

public sealed interface RetryDecision permits RetryDecision.Retry, RetryDecision.DeadLetter {
    record Retry(Duration backoff) implements RetryDecision {}
    record DeadLetter() implements RetryDecision {}
}
