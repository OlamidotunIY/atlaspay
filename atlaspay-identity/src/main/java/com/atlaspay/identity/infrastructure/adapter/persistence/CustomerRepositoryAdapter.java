package com.atlaspay.identity.infrastructure.adapter.persistence;

import com.atlaspay.identity.domain.model.Customer;
import com.atlaspay.identity.domain.repository.CustomerRepository;
import com.atlaspay.identity.infrastructure.entity.CustomerJpaEntity;
import com.atlaspay.identity.infrastructure.repository.SpringDataCustomerRepository;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository jpaRepository;
    private final com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator;

    public CustomerRepositoryAdapter(SpringDataCustomerRepository jpaRepository, com.atlaspay.shared.infrastructure.DomainSequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = toEntity(customer);
        jpaRepository.save(entity);
        return customer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByMerchantIdAndEmail(Long merchantId, String email) {
        return jpaRepository.findByIntegrationAndEmail(merchantId, email).map(this::toDomain);
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("customer_seq");
    }

    private CustomerJpaEntity toEntity(Customer domain) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setIntegration(domain.getIntegration());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail().value());
        entity.setPhone(domain.getPhone() != null ? domain.getPhone().value() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private Customer toDomain(CustomerJpaEntity entity) {
        Customer customer = new Customer(
                entity.getId(),
                entity.getCode(),
                entity.getIntegration(),
                entity.getFirstName(),
                entity.getLastName(),
                new EmailAddress(entity.getEmail()),
                entity.getPhone() != null ? new PhoneNumber(entity.getPhone()) : null,
                null // metadata mapping not fully implemented in entity yet
        );
        customer.pullDomainEvents();
        return customer;
    }
}
