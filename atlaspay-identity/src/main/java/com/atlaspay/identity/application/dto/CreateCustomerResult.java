package com.atlaspay.identity.application.dto;

import com.atlaspay.shared.domain.id.CustomerId;

public record CreateCustomerResult(
    CustomerId customerId
) {}
