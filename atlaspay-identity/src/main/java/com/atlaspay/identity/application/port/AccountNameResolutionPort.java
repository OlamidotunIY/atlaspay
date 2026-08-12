package com.atlaspay.identity.application.port;

public interface AccountNameResolutionPort {
    String resolve(String bankCode, String accountNumber);
}
