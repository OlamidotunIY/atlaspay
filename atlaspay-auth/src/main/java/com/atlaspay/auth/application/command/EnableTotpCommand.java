package com.atlaspay.auth.application.command;

public record EnableTotpCommand(
        Long authAccountId
) {}
