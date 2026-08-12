package com.atlaspay.identity.application.port.out;

public interface AccountNameResolutionPort {
    String resolve(String bankCode, String accountNumber);
}
