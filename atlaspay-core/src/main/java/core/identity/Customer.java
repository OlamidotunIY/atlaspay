package core.identity;

import core.identity.events.CustomerKycDecided;
import core.identity.events.CustomerKycTierChanged;
import core.identity.events.CustomerOnboarded;
import core.identity.valueobject.*;
import core.shared.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public final class Customer extends AggregateRoot<CustomerId> {

    private final CompanyId companyId;
    private PersonalDetails personalDetails;        // name/DOB/address VO
    private KycTier kycTier;
    private KycStatus kycStatus;

    public Customer(CustomerId id, CompanyId companyId, PersonalDetails personalDetails) {
        super(id);
        this.companyId = companyId;
        this.personalDetails = personalDetails;
        this.kycTier = KycTier.TIER_0;
        this.kycStatus = KycStatus.NOT_STARTED;

        register(new CustomerOnboarded(UUID.randomUUID(), Instant.now(), id, companyId));
    }

    public Customer(CustomerId id, CompanyId companyId, PersonalDetails personalDetails, KycTier kycTier, KycStatus kycStatus) {
        super(id);
        this.companyId = companyId;
        this.personalDetails = personalDetails;
        this.kycTier = kycTier;
        this.kycStatus = kycStatus;
    }

    public static Customer rehydrate(CustomerId id, CompanyId companyId, PersonalDetails personalDetails, KycTier kycTier, KycStatus kycStatus) {
        return new Customer(id, companyId, personalDetails, kycTier, kycStatus);
    }

    public void applyKycTier(KycTier newTier) {
        if (this.kycStatus == KycStatus.APPROVED) {
            if (newTier.ordinal() > this.kycTier.ordinal()) {
                this.kycTier = newTier;
                register(new CustomerKycTierChanged(UUID.randomUUID(), Instant.now(), this.id(), this.kycTier, newTier));
            } else {
                throw new IllegalArgumentException("Cannot downgrade KYC tier");
            }
        }
    }

    public void recordKycDecision(KycDecision decision) {
        if (decision.outcome() == KycStatus.APPROVED) {
            this.kycStatus = KycStatus.APPROVED;
        } else if (decision.outcome() == KycStatus.REJECTED) {
            this.kycStatus = KycStatus.REJECTED;
        } else {
            throw new IllegalArgumentException("Invalid KYC decision outcome");
        }
        register(new CustomerKycDecided(UUID.randomUUID(), Instant.now(), this.id(), decision.outcome()));
    }

    public KycTier kycTier() {
        return this.kycTier;
    }

    public CompanyId getCompanyId() {
        return companyId;
    }

    public PersonalDetails getPersonalDetails() {
        return personalDetails;
    }

    public KycTier getKycTier() {
        return kycTier;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }
}
