package persistence.identity;

import core.identity.valueobject.KycEvaluationSagaState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "kyc_evaluation_sagas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KycEvaluationSagaJpaEntity {
    @Id
    private UUID id;
    private UUID customerId;
    @ElementCollection
    @CollectionTable(name = "kyc_evaluation_saga_required_checks", joinColumns = @JoinColumn(name = "saga_id"))
    @Column(name = "check_name")
    private Set<String> requiredChecks;

    @ElementCollection
    @CollectionTable(name = "kyc_evaluation_saga_completed_checks", joinColumns = @JoinColumn(name = "saga_id"))
    @Column(name = "check_name")
    private Set<String> completedChecks;
    @Enumerated(EnumType.STRING)
    private KycEvaluationSagaState state;
    @Version
    private long version;

    public KycEvaluationSagaJpaEntity(UUID id, UUID customerId, Set<String> requiredChecks, Set<String> completedChecks, KycEvaluationSagaState state) {
        this.id = id;
        this.customerId = customerId;
        this.requiredChecks = requiredChecks;
        this.completedChecks = completedChecks;
        this.state = state;
    }
}
