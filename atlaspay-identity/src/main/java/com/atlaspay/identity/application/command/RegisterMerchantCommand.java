package com.atlaspay.identity.application.command;

import com.atlaspay.identity.domain.model.BusinessType;

public record RegisterMerchantCommand(
    String country,
    String businessName,
    String firstName,
    String lastName,
    String email,
    String phone,
    BusinessType businessType
) {}

