package persistence.identity;

import core.identity.valueobject.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataKycCaseRepository extends JpaRepository<KycCaseJpaEntity, UUID> {
    Optional<KycCaseJpaEntity> findActiveByCustomerId(CustomerId customerId);
}
