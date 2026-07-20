package core.identity;

import core.identity.events.KycCaseOpened;
import core.identity.events.KycCheckResultRecorded;
import core.identity.valueobject.CustomerId;
import core.identity.valueobject.KycCaseId;
import core.identity.valueobject.KycCheckResult;
import core.identity.valueobject.KycStatus;
import core.shared.AggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class KycCase extends AggregateRoot<KycCaseId> {

    private final CustomerId customerId;
    private final List<KycCheckResult> checkResults;
    private KycStatus status;

    public KycCase(KycCaseId id, CustomerId customerId) {
        super(id);
        this.customerId = customerId;
        this.checkResults = new ArrayList<>();
        this.status = KycStatus.NOT_STARTED;

        register(new KycCaseOpened(UUID.randomUUID(), Instant.now(), this.id(), customerId));
    }

    public void recordCheckResult(KycCheckResult result) {
        Objects.requireNonNull(result, "result must not be null");

        checkResults.add(result);
        recalculateStatus();

        register(new KycCheckResultRecorded(
                UUID.randomUUID(),
                Instant.now(),
                id(),
                result.checkName(),
                result.passed()
        ));
    }

    private void recalculateStatus() {
        if (checkResults.isEmpty()) {
            status = KycStatus.NOT_STARTED;
            return;
        }

        boolean anyFailed = checkResults.stream().anyMatch(r -> !r.passed());
        status = anyFailed ? KycStatus.REJECTED : KycStatus.IN_REVIEW;
    }

    public List<KycCheckResult> checkResults() {
        return List.copyOf(checkResults);
    }
}
