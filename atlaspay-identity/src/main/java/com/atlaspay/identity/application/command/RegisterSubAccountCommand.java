package com.atlaspay.identity.application.command;


public record RegisterSubAccountCommand(
    Long merchantId,
    String bankCode,
    String accountNumber,
    String description
) {}
