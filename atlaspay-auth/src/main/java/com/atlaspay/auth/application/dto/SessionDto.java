package com.atlaspay.auth.application.dto;

import com.atlaspay.auth.domain.model.SessionStatus;
import java.time.ZonedDateTime;

public record SessionDto(
    Long id,
    String token,
    String ipAddress,
    String userAgent,
    SessionStatus status,
    ZonedDateTime createdAt,
    ZonedDateTime expiresAt,
    ZonedDateTime revokedAt
) {}
