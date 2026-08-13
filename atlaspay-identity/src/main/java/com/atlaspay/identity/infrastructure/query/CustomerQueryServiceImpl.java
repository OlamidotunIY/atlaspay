package com.atlaspay.identity.infrastructure.query;

import com.atlaspay.identity.application.dto.CustomerDto;
import com.atlaspay.identity.application.port.CustomerQueryService;
import com.atlaspay.identity.infrastructure.repository.SpringDataCustomerRepository;
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
    public Optional<CustomerDto> findById(Long merchantId, Long customerId) {
        return repository.findById(customerId)
                .filter(c -> c.getIntegration().equals(merchantId))
                .map(c -> new CustomerDto(
                        c.getId(),
                        c.getCode(),
                        c.getIntegration(),
                        c.getFirstName(),
                        c.getLastName(),
                        c.getEmail(),
                        c.getPhone(),
                        null,
                        c.getCreatedAt()
                ));
    }

    @Override
    public PageResult<CustomerDto> findAllByMerchantId(Long merchantId, int page, int size, String emailFilter) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }
}
