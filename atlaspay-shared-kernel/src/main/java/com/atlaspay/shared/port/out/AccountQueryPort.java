package com.atlaspay.shared.port.out;

import java.util.List;
import java.util.Optional;

public interface AccountQueryPort {
    Optional<AccountDetailsDto> findAccountDetails(Long accountId);
    List<AccountDetailsDto> findAccountsByIntegration(Long integration);
}
