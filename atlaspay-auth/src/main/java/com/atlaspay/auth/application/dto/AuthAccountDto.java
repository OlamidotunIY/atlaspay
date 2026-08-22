package com.atlaspay.auth.application.dto;

import com.atlaspay.auth.domain.model.AuthProvider;
import com.atlaspay.auth.domain.model.AuthStatus;
import com.atlaspay.auth.domain.model.PrincipalType;

public record AuthAccountDto(
    Long id,
    Long principalId,
    PrincipalType principalType,
    AuthProvider provider,
    String scope,
    boolean totpEnabled,
    AuthStatus status
) {}
