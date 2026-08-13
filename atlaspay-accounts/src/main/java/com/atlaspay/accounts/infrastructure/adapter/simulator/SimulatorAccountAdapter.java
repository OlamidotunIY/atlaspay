package com.atlaspay.accounts.infrastructure.adapter.simulator;

import com.atlaspay.accounts.application.dto.AccountIssuanceRequestDto;
import com.atlaspay.accounts.application.port.out.AccountIssuancePort;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import com.atlaspay.shared.exception.BusinessRuleException;
import com.atlaspay.accounts.domain.exception.AccountsErrorCode;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SimulatorAccountAdapter implements AccountIssuancePort {

    private final Random random = new Random();

    @Override
    public NUBAN issueVirtualAccount(AccountIssuanceRequestDto request) {
        if (!"NG".equalsIgnoreCase(request.country()) && !"Nigeria".equalsIgnoreCase(request.country())) {
            throw new BusinessRuleException(AccountsErrorCode.UNSUPPORTED_COUNTRY, "System only supports Nigerian merchants");
        }

        if (!"Wema".equalsIgnoreCase(request.bankName()) && !"Zenith".equalsIgnoreCase(request.bankName())) {
            throw new BusinessRuleException(AccountsErrorCode.UNSUPPORTED_BANK, "Simulator only supports Wema and Zenith banks");
        }

        // Generate a random 10-digit NUBAN. Real NUBAN has check digits, but simple random is fine for simulator
        long number = (long) (random.nextDouble() * 10000000000L);
        String generatedNuban = String.format("%010d", number);
        
        return new NUBAN(generatedNuban);
    }
}
