package com.atlaspay.identity.application.dto;

import com.atlaspay.shared.domain.id.MerchantId;

public record RegisterMerchantResult(
    MerchantId merchantId,
    String testPublicKey,
    String testSecretKey
) {}
