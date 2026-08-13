package com.atlaspay.identity.domain.repository;

import com.atlaspay.identity.domain.model.Customer;

import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    Optional<Customer> findByMerchantIdAndEmail(Long merchantId, String email);
}
