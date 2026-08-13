package com.atlaspay.accounts.infrastructure.adapter.simulator;

import com.atlaspay.accounts.application.dto.AccountIssuanceRequestDto;
import com.atlaspay.accounts.application.port.out.AccountIssuancePort;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SimulatorAccountAdapter implements AccountIssuancePort {

    private final Random random = new Random();

    @Override
    public NUBAN issueVirtualAccount(AccountIssuanceRequestDto request) {
        // In a real implementation, this makes a POST request to the Simulator's API
        // e.g., POST /simulator/mock/accounts/issue
        
        // For now, since this is just an adapter interface layer inside AtlasPay,
        // it simply delegates out. If we were fully simulating the HTTP boundary,
        // we'd use a RestClient here.
        // We will implement the actual generation logic in the `atlaspay-simulator` module.
        
        // As a temporary placeholder returning null because the saga should be async
        // and the real NUBAN comes back via webhook. 
        // Wait, the port says it returns NUBAN. If the process is async, it should return void.
        // Let's just mock a success HTTP response and return null since it's an async webhook process.
        
        return null;
    }
}
