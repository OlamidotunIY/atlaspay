package com.atlaspay.identity.application.command;

import com.atlaspay.shared.domain.id.MerchantId;
import java.util.Map;

public record CreateCustomerCommand(
    MerchantId merchantId,
    String firstName,
    String lastName,
    String email,
    String phone,
    Map<String, String> metadata
) {}
