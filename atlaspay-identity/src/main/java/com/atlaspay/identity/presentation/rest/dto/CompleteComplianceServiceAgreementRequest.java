package com.atlaspay.identity.presentation.rest.dto;

import jakarta.validation.constraints.AssertTrue;

public record CompleteComplianceServiceAgreementRequest(
    @AssertTrue(message = "You must agree to the terms")
    boolean agreed
) {}
