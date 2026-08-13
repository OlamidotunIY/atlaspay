package com.atlaspay.identity.application.dto;

import java.time.ZonedDateTime;

public record SubAccountDto(
    Long id,
    Long integration,
    String bankCode,
    String accountNumber,
    String accountName,
    String description,
    boolean active,
    ZonedDateTime createdAt
) {}
