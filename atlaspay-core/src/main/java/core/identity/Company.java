package core.identity;

import core.identity.events.CompanyRegistered;
import core.identity.events.CompanySuspended;
import core.identity.events.CompanyVerified;
import core.identity.valueobject.*;
import core.shared.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public final class Company extends AggregateRoot<CompanyId> {

    private CompanyName name;
    private RegistrationNumber registrationNumber;
    private CompanyStatus status;
    private final Instant onboardedAt;

    public Company(CompanyId id, CompanyName name, RegistrationNumber regNo) {
        super(id);
        this.name = name;
        this.registrationNumber = regNo;
        onboardedAt = Instant.now();
        this.status = CompanyStatus.PENDING;

        register(new CompanyRegistered(UUID.randomUUID(), Instant.now(), id, name));
    }

    private Company(
            CompanyId id,
            CompanyName name,
            RegistrationNumber registrationNumber,
            CompanyStatus status,
            Instant onboardedAt
    ) {
        super(id);
        this.name = name;
        this.registrationNumber = registrationNumber;
        this.status = status;
        this.onboardedAt = onboardedAt;
    }

    public static Company rehydrate(
            CompanyId id,
            CompanyName name,
            RegistrationNumber registrationNumber,
            CompanyStatus status,
            Instant onboardedAt
    ) {
        return new Company(
                id,
                name,
                registrationNumber,
                status,
                onboardedAt
        );
    }

    public void verify(VerificationDecision decision) {
        if (this.status == CompanyStatus.PENDING) {
            if (decision.approved()) {
                this.status = CompanyStatus.VERIFIED;
                register(new CompanyVerified(UUID.randomUUID(), Instant.now(), this.id()));
            }
        }
    }

    public void suspend(String reason) {
        if (this.status == CompanyStatus.VERIFIED) {
            this.status = CompanyStatus.SUSPENDED;
            register(new CompanySuspended(UUID.randomUUID(), Instant.now(), this.id(), reason));
        }
    }

    public CompanyStatus status() {
        return this.status;
    }

    public CompanyName getName() {
        return name;
    }

    public RegistrationNumber getRegistrationNumber() {
        return registrationNumber;
    }

    public CompanyStatus getStatus() {
        return status;
    }

    public Instant getOnboardedAt() {
        return onboardedAt;
    }
}
