package com.atlaspay.identity.infrastructure.adapter.security;

import com.atlaspay.identity.application.port.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

    private final org.springframework.security.crypto.password.PasswordEncoder delegate;

    public BCryptPasswordEncoderAdapter() {
        this.delegate = new BCryptPasswordEncoder();
    }

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
