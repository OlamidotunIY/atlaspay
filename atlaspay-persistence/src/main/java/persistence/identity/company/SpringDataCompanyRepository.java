package persistence.identity.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataCompanyRepository extends JpaRepository<CompanyJpaEntity, UUID> {

    Optional<CompanyJpaEntity> findByRegistrationNumber(String registrationNumber);
}
