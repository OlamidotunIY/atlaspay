package com.atlaspay.identity.application.dto;

import java.time.ZonedDateTime;
import java.util.Map;

public record CustomerDto(
    Long id,
    String code,
    Long integration,
    String firstName,
    String lastName,
    String email,
    String phone,
    Map<String, String> metadata,
    ZonedDateTime createdAt
) {}
