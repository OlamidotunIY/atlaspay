package com.atlaspay.auth.infrastructure.adapter.security;

import com.atlaspay.auth.application.port.out.TokenGeneratorPort;
import com.atlaspay.auth.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {

    private final Key key;
    private final JwtProperties jwtProperties;

    public JwtTokenGeneratorAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    @Override
    public TokenData generateAccessToken(Long principalId, String principalType, String scope) {
        String jti = UUID.randomUUID().toString();
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(jwtProperties.getAccessTokenExpirationMs() * 1000000);
        
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
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(jwtProperties.getRefreshTokenExpirationMs() * 1000000);

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
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(jwtProperties.getPreAuthTokenExpirationMs() * 1000000);

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
