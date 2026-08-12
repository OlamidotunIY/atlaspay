package com.atlaspay.identity.domain.repository;

import com.atlaspay.identity.domain.model.Customer;
import com.atlaspay.shared.domain.id.CustomerId;
import com.atlaspay.shared.domain.id.MerchantId;

import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(CustomerId id);
    Optional<Customer> findByMerchantIdAndEmail(MerchantId merchantId, String email);
}
