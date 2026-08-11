package com.atlaspay.identity.application.dto;

import com.atlaspay.shared.domain.id.SubAccountId;

public record RegisterSubAccountResult(
    SubAccountId subAccountId,
    String accountName
) {}
