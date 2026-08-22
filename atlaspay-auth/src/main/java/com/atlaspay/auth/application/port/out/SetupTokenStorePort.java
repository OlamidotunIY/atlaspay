package com.atlaspay.auth.application.port.out;

import java.util.Optional;

public interface SetupTokenStorePort {
    void store(String token, Long authAccountId);
    Optional<Long> consume(String token);
}