package com.atlaspay.shared.port.out;

import com.atlaspay.shared.money.CurrencyCode;

public record AccountDetailsDto(
        Long accountId,
        Long integration,
        CurrencyCode currency,
        String status
) {}
