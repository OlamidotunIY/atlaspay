package persistence.identity.company;

import core.identity.Company;
import core.identity.valueobject.CompanyId;
import core.identity.valueobject.CompanyName;
import core.identity.valueobject.RegistrationNumber;

public final class CompanyMapper {
    public CompanyJpaEntity toJpaEntity(Company domain) {
        return new CompanyJpaEntity(
                domain.id().value(),
                domain.getName().value(),
                domain.getRegistrationNumber().value(),
                domain.getStatus(),
                domain.getOnboardedAt()
        );
    }


    public Company toDomain(CompanyJpaEntity entity) {
        return Company.rehydrate(
                new CompanyId(entity.getId()),
                new CompanyName(entity.getName()),
                new RegistrationNumber(entity.getRegistrationNumber()),
                entity.getStatus(),
                entity.getOnboardedAt()
        );
    }
}
