package com.atlaspay.auth.application.command;

import com.atlaspay.auth.domain.model.VerificationType;

public record CompleteVerificationCommand(
        VerificationType type,
        String identifier,
        String code
) {}
