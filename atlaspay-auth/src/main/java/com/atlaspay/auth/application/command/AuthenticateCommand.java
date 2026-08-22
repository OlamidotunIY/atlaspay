package com.atlaspay.auth.application.command;

import com.atlaspay.auth.domain.model.PrincipalType;

public record AuthenticateCommand(
        Long principalId,
        PrincipalType principalType,
        String rawCredential,
        String ipAddress,
        String userAgent
) {}
