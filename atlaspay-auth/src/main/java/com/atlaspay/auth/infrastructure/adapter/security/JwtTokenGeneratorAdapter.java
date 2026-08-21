package com.atlaspay.auth.infrastructure.adapter.security;

import com.atlaspay.auth.application.port.out.TokenGeneratorPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {

    private final Key key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final long preAuthTokenExpirationMs;

    public JwtTokenGeneratorAdapter(
            @Value("${atlaspay.auth.jwt.secret:defaultSecretKeyWithAtLeast32CharactersForHmacSha256}") String secret,
            @Value("${atlaspay.auth.jwt.access-token-expiration-ms:900000}") long accessTokenExpirationMs,
            @Value("${atlaspay.auth.jwt.refresh-token-expiration-ms:604800000}") long refreshTokenExpirationMs,
            @Value("${atlaspay.auth.jwt.pre-auth-token-expiration-ms:300000}") long preAuthTokenExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.preAuthTokenExpirationMs = preAuthTokenExpirationMs;
    }

    @Override
    public TokenData generateAccessToken(Long principalId, String principalType, String scope) {
        String jti = UUID.randomUUID().toString();
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(accessTokenExpirationMs * 1000000);
        
        String token = Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(principalId))
                .claim("type", principalType)
                .claim("scope", scope)
                .claim("purpose", "access")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expiresAt.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new TokenData(token, jti, expiresAt);
    }

    @Override
    public TokenData generateRefreshToken(Long principalId, String principalType, String jti) {
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(refreshTokenExpirationMs * 1000000);

        String token = Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(principalId))
                .claim("type", principalType)
                .claim("purpose", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expiresAt.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new TokenData(token, jti, expiresAt);
    }

    @Override
    public TokenData generatePreAuthToken(Long principalId, String principalType) {
        String jti = UUID.randomUUID().toString();
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(preAuthTokenExpirationMs * 1000000);

        String token = Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(principalId))
                .claim("type", principalType)
                .claim("purpose", "pre-auth")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expiresAt.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new TokenData(token, jti, expiresAt);
    }
}
