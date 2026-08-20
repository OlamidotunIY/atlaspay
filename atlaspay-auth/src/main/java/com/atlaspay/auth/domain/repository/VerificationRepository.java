package com.atlaspay.auth.domain.repository;

import com.atlaspay.auth.domain.model.Verification;
import com.atlaspay.auth.domain.model.VerificationStatus;
import com.atlaspay.auth.domain.model.VerificationType;

import java.util.List;
import java.util.Optional;

public interface VerificationRepository {
    Long nextIdentity();
    Verification save(Verification verification);
    Optional<Verification> findById(Long id);
    Optional<Verification> findActiveByTypeAndValue(VerificationType type, String value);
    List<Verification> findByStatus(VerificationStatus status);
    void invalidatePreviousVerifications(VerificationType type, String value);
}
