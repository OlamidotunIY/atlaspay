package com.atlaspay.identity.application.dto;

import java.time.ZonedDateTime;

public record MerchantProfileDto(
    String merchantId,
    String businessName,
    String email,
    String phone,
    String kycStatus,
    ZonedDateTime createdAt
) {}
