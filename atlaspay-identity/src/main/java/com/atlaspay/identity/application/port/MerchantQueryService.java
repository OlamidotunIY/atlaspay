package com.atlaspay.identity.application.port;

import com.atlaspay.identity.application.dto.MerchantProfileDto;
import com.atlaspay.shared.domain.id.MerchantId;

import java.util.Optional;

public interface MerchantQueryService {
    Optional<MerchantProfileDto> findProfileById(MerchantId merchantId);
}
