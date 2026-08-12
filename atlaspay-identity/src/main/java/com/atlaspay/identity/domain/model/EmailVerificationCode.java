package com.atlaspay.identity.domain.model;

import com.atlaspay.identity.domain.exception.IdentityErrorCode;
import com.atlaspay.shared.exception.BusinessRuleException;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Random;

@Getter
public class EmailVerificationCode {

    private final String code;
    private final ZonedDateTime expiresAt;

    public EmailVerificationCode() {
        this.code = generateRandom6DigitCode();
        this.expiresAt = ZonedDateTime.now().plusHours(24);
    }

    private String generateRandom6DigitCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    public void validate(String inputCode) {
        if (!this.code.equals(inputCode)) {
            throw new BusinessRuleException(IdentityErrorCode.EMAIL_CODE_INVALID_OR_EXPIRED, "Invalid verification code");
        }
        if (ZonedDateTime.now().isAfter(this.expiresAt)) {
            throw new BusinessRuleException(IdentityErrorCode.EMAIL_CODE_INVALID_OR_EXPIRED, "Verification code has expired");
        }
    }
}
