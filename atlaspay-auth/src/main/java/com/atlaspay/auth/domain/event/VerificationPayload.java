package com.atlaspay.auth.domain.event;

import com.atlaspay.auth.domain.model.VerificationType;

public record VerificationPayload(
        String identifier,
        String value,
        VerificationType type
) {}
