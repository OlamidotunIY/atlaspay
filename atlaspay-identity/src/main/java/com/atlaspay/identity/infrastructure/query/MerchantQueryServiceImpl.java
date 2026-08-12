package com.atlaspay.identity.infrastructure.query;

import com.atlaspay.identity.application.dto.MerchantProfileDto;
import com.atlaspay.identity.application.port.MerchantQueryService;
import com.atlaspay.identity.infrastructure.repository.SpringDataMerchantRepository;
import com.atlaspay.shared.domain.id.MerchantId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MerchantQueryServiceImpl implements MerchantQueryService {

    private final SpringDataMerchantRepository repository;

    public MerchantQueryServiceImpl(SpringDataMerchantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<MerchantProfileDto> findProfileById(MerchantId merchantId) {
        return repository.findById(merchantId.value())
                .map(entity -> new MerchantProfileDto(
                        entity.getId(),
                        entity.getBusinessName(),
                        entity.getEmail(),
                        entity.getPhone(),
                        entity.getComplianceStatus().name(),
                        entity.getCreatedAt()
                ));
    }
}
