package com.atlaspay.auth.application.command;

public record SetupPasswordCommand(
    String setupToken,
    String newPassword,
    String ipAddress,
    String userAgent
) {}