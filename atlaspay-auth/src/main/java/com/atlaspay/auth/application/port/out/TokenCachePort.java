package com.atlaspay.auth.application.port.out;

import java.time.ZonedDateTime;

public interface TokenCachePort {
    void cacheSession(String jti, ZonedDateTime expiresAt);
    void blacklistToken(String jti, ZonedDateTime expiresAt);
    boolean isBlacklisted(String jti);
}
