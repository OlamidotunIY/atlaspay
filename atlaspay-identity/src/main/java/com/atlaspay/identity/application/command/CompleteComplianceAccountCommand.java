package com.atlaspay.identity.application.command;


public record CompleteComplianceAccountCommand(
    Long merchantId,
    String settlementBankCode,
    String settlementAccountNumber
) {}
