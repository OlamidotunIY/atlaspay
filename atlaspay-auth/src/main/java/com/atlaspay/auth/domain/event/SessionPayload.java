package com.atlaspay.auth.domain.event;

import java.time.ZonedDateTime;

public record SessionPayload(
    String token,
    ZonedDateTime expiresAt
) {}
