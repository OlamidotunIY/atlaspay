package com.atlaspay.identity.application.port;

import com.atlaspay.identity.application.dto.ApiKeyDto;

import java.util.List;

public interface ApiKeyQueryService {
    List<ApiKeyDto> findAllByIntegration(Long merchantId);
}
