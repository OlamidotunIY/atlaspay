package com.atlaspay.auth.application.command;

public record RevokeAllSessionsCommand(
        Long authAccountId
) {}
