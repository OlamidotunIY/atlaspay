package com.atlaspay.auth.application.port.out;

public interface TotpServicePort {
    String generateSecret();
    String generateUri(String secret, String accountName);
    boolean verify(String secret, String code);
}
