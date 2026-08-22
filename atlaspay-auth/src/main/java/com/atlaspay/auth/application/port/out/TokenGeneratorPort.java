package com.atlaspay.auth.application.port.out;

import java.time.ZonedDateTime;
import java.util.Map;

public interface TokenGeneratorPort {
    TokenData generateAccessToken(Long principalId, String principalType, String scope);
    TokenData generateRefreshToken(Long principalId, String principalType, String jti);
    TokenData generatePreAuthToken(Long principalId, String principalType);

    record TokenData(String token, String jti, ZonedDateTime expiresAt) {}
}
