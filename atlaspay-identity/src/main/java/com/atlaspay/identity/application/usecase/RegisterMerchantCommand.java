package com.atlaspay.identity.application.usecase;

import com.atlaspay.identity.domain.model.BusinessType;

public record RegisterMerchantCommand(
    String country,
    String businessName,
    String firstName,
    String lastName,
    String email,
    String phone,
    String password,
    BusinessType businessType
) {}
