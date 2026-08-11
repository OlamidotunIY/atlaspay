package com.atlaspay.identity.application.dto;

import java.time.ZonedDateTime;
import java.util.Map;

public record CustomerDto(
    String customerId,
    String merchantId,
    String firstName,
    String lastName,
    String email,
    String phone,
    Map<String, String> metadata,
    ZonedDateTime createdAt
) {}
