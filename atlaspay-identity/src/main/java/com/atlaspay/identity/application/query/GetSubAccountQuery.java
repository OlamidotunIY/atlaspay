package com.atlaspay.identity.application.query;


public record GetSubAccountQuery(
    Long merchantId,
    Long subAccountId
) {}
