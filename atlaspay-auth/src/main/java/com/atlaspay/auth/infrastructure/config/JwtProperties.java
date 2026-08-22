package com.atlaspay.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "atlaspay.auth.jwt")
public class JwtProperties {
    private String secret = "defaultSecretKeyWithAtLeast32CharactersForHmacSha256";
    private long accessTokenExpirationMs = 900000; // 15 minutes
    private long refreshTokenExpirationMs = 604800000; // 7 days
    private long preAuthTokenExpirationMs = 300000; // 5 minutes
}
