package com.atlaspay.auth.domain.service;

public interface VerificationCodeHasher {
    boolean matches(String rawCode, String hashedCode);
}
