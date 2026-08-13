package com.atlaspay.identity.application.command;

import java.util.Map;

public record CreateCustomerCommand(
    Long merchantId,
    String firstName,
    String lastName,
    String email,
    String phone,
    Map<String, String> metadata
) {}
