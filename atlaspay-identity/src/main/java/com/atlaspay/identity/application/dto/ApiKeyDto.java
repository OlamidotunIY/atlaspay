package com.atlaspay.identity.application.dto;

import java.time.ZonedDateTime;

public record ApiKeyDto(
    String keyId,
    String keyType,
    String environment,
    String displayValue,
    boolean active,
    ZonedDateTime createdAt
) {}
