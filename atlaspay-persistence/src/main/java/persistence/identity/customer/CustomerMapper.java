package persistence.identity.customer;

import core.identity.Customer;
import core.identity.valueobject.*;

public final class CustomerMapper {
    public CustomerJpaEntity toJpaEntity(Customer domain) {
        return new CustomerJpaEntity(
                domain.id().value(),
                domain.getCompanyId().value(),
                domain.getPersonalDetails().fullName(),
                domain.getPersonalDetails().dateOfBirth(),
                domain.getPersonalDetails().address().line1(),
                domain.getPersonalDetails().address().line2(),
                domain.getPersonalDetails().address().city(),
                domain.getPersonalDetails().address().postalCode(),
                domain.getPersonalDetails().address().countryCode(),
                domain.getKycTier(),
                domain.getKycStatus()
        );
    }

    public Customer toDomain(CustomerJpaEntity entity) {
        return Customer.rehydrate(
                new CustomerId(entity.getId()),
                new CompanyId(entity.getCompanyId()),
                new PersonalDetails(
                        entity.getFullName(),
                        entity.getDateOfBirth(),
                        new Address(
                                entity.getAddressLine1(),
                                entity.getAddressLine2(),
                                entity.getCity(),
                                entity.getPostalCode(),
                                entity.getCountryCode()
                        )
                ),
                entity.getKycTier(),
                entity.getKycStatus()
        );
    }
}
