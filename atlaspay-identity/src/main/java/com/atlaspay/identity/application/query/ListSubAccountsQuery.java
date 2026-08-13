package com.atlaspay.identity.application.query;


public record ListSubAccountsQuery(
    Long merchantId,
    int page,
    int size
) {}
