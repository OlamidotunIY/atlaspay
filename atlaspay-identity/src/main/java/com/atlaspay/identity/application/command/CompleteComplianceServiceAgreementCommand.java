package com.atlaspay.identity.application.command;


public record CompleteComplianceServiceAgreementCommand(
    Long merchantId,
    boolean agreed
) {}
