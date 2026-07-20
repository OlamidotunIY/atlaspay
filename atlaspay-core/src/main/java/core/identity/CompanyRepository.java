package core.identity;

import core.identity.valueobject.CompanyId;
import core.identity.valueobject.RegistrationNumber;
import core.shared.Repository;

import java.util.Optional;

public interface CompanyRepository extends Repository<Company, CompanyId> {
    Optional<Company> findByRegistrationNumber(RegistrationNumber number); // lookup for onboarding dedup
}