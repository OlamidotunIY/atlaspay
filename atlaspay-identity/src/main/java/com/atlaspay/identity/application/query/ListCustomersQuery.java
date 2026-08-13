package com.atlaspay.identity.application.query;


public record ListCustomersQuery(
    Long merchantId,
    int page,
    int size,
    String emailFilter
) {}
