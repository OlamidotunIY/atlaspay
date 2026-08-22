package com.atlaspay.auth.application.command;

import com.atlaspay.auth.domain.model.AuthProvider;
import com.atlaspay.auth.domain.model.AuthStatus;
import com.atlaspay.auth.domain.model.PrincipalType;

public record CreateAuthAccountCommand(
        Long principalId,
        PrincipalType principalType,
        String identifier,
        String secondaryIdentifier,
        AuthProvider provider,
        String rawCredential,
        String scope,
        AuthStatus initialStatus
) {}
