package com.atlaspay.identity.application.port.out;

import com.atlaspay.identity.application.dto.ApiKeyDto;
import com.atlaspay.shared.domain.id.MerchantId;

import java.util.List;

public interface ApiKeyQueryService {
    List<ApiKeyDto> findAllByMerchantId(MerchantId merchantId);
}
