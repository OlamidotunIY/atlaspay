package persistence.identity.customer;

import core.identity.Customer;
import core.identity.valueobject.CompanyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {
    List<Customer> findByCompanyId(CompanyId companyId);
}
