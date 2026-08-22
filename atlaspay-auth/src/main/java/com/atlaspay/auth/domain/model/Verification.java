package com.atlaspay.auth.domain.model;

import com.atlaspay.auth.domain.event.VerificationCompletedEvent;
import com.atlaspay.auth.domain.event.VerificationCreatedEvent;
import com.atlaspay.auth.domain.event.VerificationPayload;
import com.atlaspay.auth.domain.exception.AuthErrorCode;
import com.atlaspay.auth.domain.service.VerificationCodeHasher;
import com.atlaspay.shared.domain.AggregateRoot;
import com.atlaspay.shared.exception.BusinessRuleException;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Verification extends AggregateRoot<Long> {

    private final Long id;
    private final Long authAccountId;
    private final String identifier;
    private final String value;
    private final String code;
    private final VerificationType type;
    private VerificationStatus status;
    private final ZonedDateTime expiresAt;
    private ZonedDateTime verifiedAt;
    private int attempts;
    private final int maxAttempts;
    private final ZonedDateTime createdAt;

    public Verification(Long id, Long authAccountId, String identifier, String value, String code, VerificationType type, VerificationStatus status, ZonedDateTime expiresAt, int attempts, int maxAttempts) {
        this.id = id;
        this.authAccountId = authAccountId;
        this.identifier = identifier;
        this.value = value;
        this.code = code;
        this.type = type;
        this.status = status;
        this.expiresAt = expiresAt;
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.createdAt = ZonedDateTime.now();
    }

    public static Verification create(Long id, Long authAccountId, String identifier, String value, String code, String rawCode, VerificationType type, ZonedDateTime expiresAt, int maxAttempts) {
        Verification verification = new Verification(id, authAccountId, identifier, value, code, type, VerificationStatus.PENDING, expiresAt, 0, maxAttempts);

        verification.registerEvent(
                new VerificationCreatedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(id),
                        ZonedDateTime.now(),
                        new VerificationPayload(identifier, value, type, rawCode)
                ));
        return verification;
    }

    public void complete(String rawCode, VerificationCodeHasher hasher) {
        if (this.status != VerificationStatus.PENDING) {
            throw new BusinessRuleException(AuthErrorCode.ACCOUNT_NOT_ACTIVE, "Verification is not pending");
        }

        if (ZonedDateTime.now().isAfter(this.expiresAt)) {
            this.status = VerificationStatus.EXPIRED;
            throw new BusinessRuleException(AuthErrorCode.VERIFICATION_EXPIRED, "Verification code has expired");
        }

        if (this.attempts >= this.maxAttempts) {
            this.status = VerificationStatus.MAX_ATTEMPTS_EXCEEDED;
            throw new BusinessRuleException(AuthErrorCode.MAX_ATTEMPTS_EXCEEDED, "Maximum verification attempts exceeded");
        }

        if (!hasher.matches(rawCode, this.code)) {
            this.attempts++;
            if (this.attempts >= this.maxAttempts) {
                this.status = VerificationStatus.MAX_ATTEMPTS_EXCEEDED;
                throw new BusinessRuleException(AuthErrorCode.MAX_ATTEMPTS_EXCEEDED, "Maximum verification attempts exceeded");
            }
            throw new BusinessRuleException(AuthErrorCode.INVALID_VERIFICATION_CODE, "Invalid verification code");
        }

        this.status = VerificationStatus.VERIFIED;
        this.verifiedAt = ZonedDateTime.now();

        this.registerEvent(
                new VerificationCompletedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(id),
                        ZonedDateTime.now(),
                        new VerificationPayload(this.identifier, this.value, this.type, null)
                ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
