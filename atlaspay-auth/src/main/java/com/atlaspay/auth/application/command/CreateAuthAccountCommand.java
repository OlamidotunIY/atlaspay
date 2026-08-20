package com.atlaspay.auth.application.command;

import com.atlaspay.auth.domain.model.AuthProvider;
import com.atlaspay.auth.domain.model.PrincipalType;

public record CreateAuthAccountCommand(
        Long principalId,
        PrincipalType principalType,
        AuthProvider provider,
        String rawCredential,
        String scope
) {}
