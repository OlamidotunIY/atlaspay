package com.atlaspay.auth.application.command;

import com.atlaspay.auth.domain.model.VerificationType;

public record CreateVerificationCommand(
        Long authAccountId,
        String identifier,
        VerificationType type
) {}
