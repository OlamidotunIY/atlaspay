package com.atlaspay.identity.application.dto;

import java.time.ZonedDateTime;

public record SubAccountDto(
    String subAccountId,
    String merchantId,
    String bankCode,
    String accountNumber,
    String accountName,
    String description,
    boolean active,
    ZonedDateTime createdAt
) {}
