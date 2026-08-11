package com.atlaspay.identity.application.usecase;

import com.atlaspay.shared.domain.id.MerchantId;
import com.atlaspay.identity.domain.model.GovernmentIdType;
import java.time.LocalDate;

public record CompleteComplianceOwnerCommand(
    MerchantId merchantId,
    String ownerBvn,
    String ownerNin,
    LocalDate ownerDateOfBirth,
    String ownerAddress,
    GovernmentIdType ownerIdType,
    String ownerIdNumber,
    String rcNumber
) {}
