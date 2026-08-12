package com.atlaspay.identity.infrastructure.query;

import com.atlaspay.identity.application.dto.SubAccountDto;
import com.atlaspay.identity.application.port.SubAccountQueryService;
import com.atlaspay.identity.infrastructure.persistence.repository.SpringDataSubAccountRepository;
import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.shared.domain.id.SubAccountId;
import com.atlaspay.shared.util.PageResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class SubAccountQueryServiceImpl implements SubAccountQueryService {

    private final SpringDataSubAccountRepository repository;

    public SubAccountQueryServiceImpl(SpringDataSubAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SubAccountDto> findById(MerchantId merchantId, SubAccountId subAccountId) {
        return repository.findById(subAccountId.value())
                .filter(s -> s.getMerchantId().equals(merchantId.value()))
                .map(s -> new SubAccountDto(
                        s.getId(),
                        s.getMerchantId(),
                        s.getBankCode(),
                        s.getAccountNumber(),
                        s.getAccountName(),
                        s.getDescription(),
                        s.isActive(),
                        s.getCreatedAt()
                ));
    }

    @Override
    public PageResult<SubAccountDto> findAllByMerchantId(MerchantId merchantId, int page, int size) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }
}
