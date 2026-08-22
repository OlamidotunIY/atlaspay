package com.atlaspay.auth.application.dto;

import java.time.ZonedDateTime;

public record AuthTokenDto(
        String accessToken,
        String refreshToken,
        ZonedDateTime accessExpiresAt,
        ZonedDateTime refreshExpiresAt
) {}
