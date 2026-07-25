package persistence.identity.kyc;

import core.identity.valueobject.KycStatus;
import core.identity.valueobject.KycTier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "kyc_cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KycCaseJpaEntity {
    @Id
    private UUID id;
    private UUID customerId;
    @Enumerated(EnumType.STRING)
    private KycTier targetTier;
    @ElementCollection
    @CollectionTable(name = "kyc_check_results", joinColumns = @JoinColumn(name = "kyc_case_id"))
    private List<KycCheckResultEmbeddable> checkResults;
    @Enumerated(EnumType.STRING)
    private KycStatus status;
    @Version
    private long version;

    public KycCaseJpaEntity(UUID id, UUID customerId, KycTier targetTier, List<KycCheckResultEmbeddable> checkResults, KycStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.targetTier = targetTier;
        this.checkResults = checkResults;
        this.status = status;
    }
}

