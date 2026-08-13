package com.atlaspay.app.security.authentication;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class AtlasPayAuthenticationToken extends AbstractAuthenticationToken {

    private final String merchantId;
    private final Object credentials;
    @Getter
    private final AuthType authType;

    public enum AuthType {
        JWT, API_KEY
    }

    public AtlasPayAuthenticationToken(String merchantId, Object credentials, AuthType authType) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_MERCHANT")));
        this.merchantId = merchantId;
        this.credentials = credentials;
        this.authType = authType;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return merchantId;
    }

}
