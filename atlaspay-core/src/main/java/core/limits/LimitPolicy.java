package core.limits;

import core.identity.valueobject.KycTier;
import core.ledger.valueobjects.Money;
import core.limits.events.LimitPolicyCreated;
import core.limits.valueobjects.LimitPolicyId;
import core.shared.AggregateRoot;
import core.shared.Result;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class LimitPolicy extends AggregateRoot<LimitPolicyId> {
    private final KycTier applicableTier;          // ties policy to a KYC tier
    private final Money maxSingleTransaction;
    private final Money maxRollingWindow;
    private final Duration rollingWindowDuration;

    public LimitPolicy(LimitPolicyId id, KycTier applicableTier, Money maxSingleTransaction, Money maxRollingWindow, Duration rollingWindowDuration) {
        super(id);
        this.applicableTier = applicableTier;
        this.maxSingleTransaction = maxSingleTransaction;
        this.maxRollingWindow = maxRollingWindow;
        this.rollingWindowDuration = rollingWindowDuration;
        register(new LimitPolicyCreated(UUID.randomUUID(), Instant.now(), id(), applicableTier));

    }

    public Result<Void, String> evaluateSingleTransaction(Money amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");

        if (amount.compareTo(maxSingleTransaction) > 0) {
            return new Result.Err<>("Amount exceeds single-transaction limit");
        }

        return new Result.Ok<>(null);
    }
}