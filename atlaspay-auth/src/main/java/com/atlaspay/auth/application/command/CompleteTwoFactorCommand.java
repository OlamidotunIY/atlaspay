package com.atlaspay.auth.application.command;

public record CompleteTwoFactorCommand(
        String preAuthToken,
        String code,
        String ipAddress,
        String userAgent
) {}
