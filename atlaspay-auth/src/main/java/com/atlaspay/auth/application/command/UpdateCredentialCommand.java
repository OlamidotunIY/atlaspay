package com.atlaspay.auth.application.command;

public record UpdateCredentialCommand(
        Long authAccountId,
        String rawNewCredential
) {}
