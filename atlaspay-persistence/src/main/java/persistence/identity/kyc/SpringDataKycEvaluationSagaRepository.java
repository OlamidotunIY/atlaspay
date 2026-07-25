package persistence.identity.kyc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataKycEvaluationSagaRepository extends JpaRepository<KycEvaluationSagaJpaEntity, UUID> {
}
