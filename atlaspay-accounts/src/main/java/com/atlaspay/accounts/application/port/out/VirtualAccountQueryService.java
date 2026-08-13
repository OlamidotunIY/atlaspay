package com.atlaspay.accounts.application.port.out;

import java.util.Optional;
import com.atlaspay.shared.domain.valueobject.NUBAN;

public interface VirtualAccountQueryService {
    int countByIntegration(Long integration);
    boolean existsByIntegrationAndBankName(Long integration, String bankName);
    Optional<String> findIntegrationByNuban(NUBAN nuban);
}
