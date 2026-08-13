package com.atlaspay.identity.application.dto;


public record RegisterMerchantResult(
    Long merchantId,
    String testPublicKey,
    String testSecretKey
) {}
