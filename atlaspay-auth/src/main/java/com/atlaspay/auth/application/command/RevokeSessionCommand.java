package com.atlaspay.auth.application.command;

public record RevokeSessionCommand(
        Long sessionId,
        Long authAccountId
) {}
