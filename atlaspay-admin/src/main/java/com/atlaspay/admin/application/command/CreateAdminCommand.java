package com.atlaspay.admin.application.command;

public record CreateAdminCommand(
    String fullName,
    String personalEmail,
    String role
) {}
