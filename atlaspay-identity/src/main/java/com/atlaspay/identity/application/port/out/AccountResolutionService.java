package com.atlaspay.identity.application.port.out;

import java.util.Optional;

public interface AccountResolutionService {
    Optional<String> resolveAccountName(String bankCode, String accountNumber);
}
