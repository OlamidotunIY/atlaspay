package com.atlaspay.identity.application.command;

import com.atlaspay.identity.domain.model.StaffSize;
import java.math.BigDecimal;

public record CompleteComplianceProfileCommand(
    Long merchantId,
    String description,
    StaffSize staffSize,
    String industry,
    String category,
    BigDecimal annualProjectedSalesVolume,
    String annualProjectedSalesCurrency
) {}
