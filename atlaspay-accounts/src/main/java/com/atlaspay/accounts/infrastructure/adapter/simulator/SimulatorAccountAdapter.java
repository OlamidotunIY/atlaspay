package com.atlaspay.accounts.infrastructure.adapter.simulator;

import com.atlaspay.accounts.application.dto.AccountIssuanceRequestDto;
import com.atlaspay.accounts.application.port.out.AccountIssuancePort;
import com.atlaspay.shared.domain.valueobject.NUBAN;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Slf4j
@Component
public class SimulatorAccountAdapter implements AccountIssuancePort {

    private final RestClient restClient;
    private final String callbackUrl;

    public SimulatorAccountAdapter(
            @Qualifier("simulatorRestClient") RestClient restClient,
            @Value("${atlaspay.simulator.callback-url:http://localhost:8080/api/v1/accounts/webhooks/simulator}") String callbackUrl) {
        this.restClient = restClient;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public NUBAN issueVirtualAccount(AccountIssuanceRequestDto request) {
        log.info("Sending virtual account issuance request to Simulator for Reference ID: {}", request.referenceId());
        
        Map<String, String> payload = Map.of(
                "referenceId", request.referenceId(),
                "accountName", request.accountName(),
                "callbackUrl", callbackUrl,
                "bankName", request.bankName() != null ? request.bankName() : "Wema Bank"
        );

        restClient.post()
                .uri("/simulator/mock/accounts/issue")
                .body(payload)
                .retrieve()
                .toBodilessEntity();
                
        log.info("Successfully dispatched issuance request to Simulator for Reference ID: {}", request.referenceId());

        // We return null because the process is asynchronous. 
        // The simulator will respond via webhook to provide the actual NUBAN.
        return null; 
    }
}
