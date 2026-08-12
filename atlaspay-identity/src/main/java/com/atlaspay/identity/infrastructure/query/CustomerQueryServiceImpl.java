package com.atlaspay.identity.infrastructure.query;

import com.atlaspay.identity.application.dto.CustomerDto;
import com.atlaspay.identity.application.port.out.CustomerQueryService;
import com.atlaspay.identity.infrastructure.persistence.repository.SpringDataCustomerRepository;
import com.atlaspay.shared.domain.id.CustomerId;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.util.PageResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomerQueryServiceImpl implements CustomerQueryService {

    private final SpringDataCustomerRepository repository;

    public CustomerQueryServiceImpl(SpringDataCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CustomerDto> findById(MerchantId merchantId, CustomerId customerId) {
        return repository.findById(customerId.value())
                .filter(c -> c.getMerchantId().equals(merchantId.value()))
                .map(c -> new CustomerDto(
                        c.getId(),
                        c.getMerchantId(),
                        c.getFirstName(),
                        c.getLastName(),
                        c.getEmail(),
                        c.getPhone(),
                        null,
                        c.getCreatedAt()
                ));
    }

    @Override
    public PageResult<CustomerDto> findAllByMerchantId(MerchantId merchantId, int page, int size, String emailFilter) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }
}
