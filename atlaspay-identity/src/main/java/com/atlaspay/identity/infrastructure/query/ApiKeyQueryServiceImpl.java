package com.atlaspay.identity.infrastructure.query;

import com.atlaspay.identity.application.dto.ApiKeyDto;
import com.atlaspay.identity.application.port.ApiKeyQueryService;
import com.atlaspay.identity.infrastructure.repository.SpringDataApiKeyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiKeyQueryServiceImpl implements ApiKeyQueryService {

    private final SpringDataApiKeyRepository repository;

    public ApiKeyQueryServiceImpl(SpringDataApiKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ApiKeyDto> findAllByIntegration(Long merchantId) {
        return repository.findAllByIntegration(merchantId)
                .stream()
                .map(k -> new ApiKeyDto(
                        k.getId(),
                        k.getKeyType().name(),
                        k.getEnvironment().name(),
                        k.getDisplayValue(),
                        k.isActive(),
                        k.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
