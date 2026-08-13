package com.atlaspay.identity.application.query;


public record GetCustomerQuery(
    Long merchantId,
    Long customerId
) {}
