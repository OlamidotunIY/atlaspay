package com.atlaspay.auth.application.command;

public record RevokeSessionCommand(
        String token
) {}
