package com.atlaspay.identity.application.port;

import com.atlaspay.identity.application.dto.MerchantProfileDto;

import java.util.Optional;

public interface MerchantQueryService {
    Optional<MerchantProfileDto> findProfileById(Long merchantId);
}
