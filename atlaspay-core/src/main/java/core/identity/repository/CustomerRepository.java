package core.identity.repository;

import core.identity.entities.Customer;
import core.identity.valueobject.CompanyId;
import core.identity.valueobject.CustomerId;
import core.shared.Repository;

import java.util.List;

public interface CustomerRepository extends Repository<Customer, CustomerId> {
    List<Customer> findByCompanyId(CompanyId companyId); // list customers under a company
}
