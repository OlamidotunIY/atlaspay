package com.atlaspay.identity.infrastructure.persistence;

import com.atlaspay.identity.domain.model.Customer;
import com.atlaspay.identity.domain.repository.CustomerRepository;
import com.atlaspay.identity.infrastructure.persistence.entity.CustomerJpaEntity;
import com.atlaspay.identity.infrastructure.persistence.repository.SpringDataCustomerRepository;
import com.atlaspay.shared.domain.id.CustomerId;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.valueobject.EmailAddress;
import com.atlaspay.shared.domain.valueobject.PhoneNumber;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository jpaRepository;

    public CustomerRepositoryAdapter(SpringDataCustomerRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = toEntity(customer);
        jpaRepository.save(entity);
        return customer;
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByMerchantIdAndEmail(MerchantId merchantId, String email) {
        return jpaRepository.findByMerchantIdAndEmail(merchantId.value(), email).map(this::toDomain);
    }

    private CustomerJpaEntity toEntity(Customer domain) {
        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setId(domain.getId().value());
        entity.setMerchantId(domain.getMerchantId().value());
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
                new CustomerId(entity.getId()),
                new MerchantId(entity.getMerchantId()),
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
