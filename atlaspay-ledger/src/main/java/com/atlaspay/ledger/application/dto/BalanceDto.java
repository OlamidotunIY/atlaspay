package com.atlaspay.ledger.application.dto;

import java.math.BigDecimal;

public record BalanceDto(
    String currency,
    BigDecimal balance
) {}
