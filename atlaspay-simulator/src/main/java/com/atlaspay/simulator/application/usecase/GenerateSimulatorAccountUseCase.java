package com.atlaspay.simulator.application.usecase;

import com.atlaspay.shared.usecase.BaseUseCase;
import com.atlaspay.simulator.application.command.GenerateSimulatorAccountCommand;
import com.atlaspay.simulator.domain.repository.SimulatorAccountRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GenerateSimulatorAccountUseCase extends BaseUseCase<GenerateSimulatorAccountCommand, Void> {

    private final SimulatorAccountRepository repository;

    @Override
    public Void execute(GenerateSimulatorAccountCommand command) {
        String bankCode = determineBankCode(command.bankName());
        
        long accountSerial = repository.getNextAccountSerial();
        
        String serialStr = String.format("%09d", accountSerial);
        String checkDigit = calculateCheckDigit(bankCode, serialStr);
        String nuban = serialStr + checkDigit;
        
        repository.saveAccount(
                UUID.randomUUID().toString(),
                command.referenceId(),
                command.bankName(),
                bankCode,
                accountSerial,
                nuban,
                command.accountName(),
                command.callbackUrl(),
                "COMPLETED"
        );
        
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(java.time.Duration.ofSeconds(new java.util.Random().nextInt(3) + 1));
                
                java.util.Map<String, Object> webhookPayload = java.util.Map.of(
                        "event", "virtual_account.created",
                        "data", java.util.Map.of(
                                "reference", command.referenceId(),
                                "nuban", nuban
                        )
                );

                org.springframework.web.client.RestClient.create().post()
                        .uri(command.callbackUrl())
                        .header("X-Simulator-Signature", "dummy")
                        .body(webhookPayload)
                        .retrieve()
                        .toBodilessEntity();
                        
                System.out.println("Simulator successfully fired webhook to " + command.callbackUrl());
            } catch (Exception e) {
                System.err.println("Simulator failed to fire webhook: " + e.getMessage());
            }
        });
        
        return null;
    }

    private String determineBankCode(String bankName) {
        if ("Wema".equalsIgnoreCase(bankName)) {
            return "035";
        } else if ("Zenith".equalsIgnoreCase(bankName)) {
            return "057";
        }
        return "999"; // Fallback or mock
    }

    private String calculateCheckDigit(String bankCode, String serialNumber) {
        // Simple CBN Modulo 10 implementation for NUBAN
        String combined = bankCode + serialNumber;
        int[] weights = {3, 7, 3, 3, 7, 3, 3, 7, 3, 3, 7, 3};
        int sum = 0;
        
        for (int i = 0; i < combined.length(); i++) {
            sum += Character.getNumericValue(combined.charAt(i)) * weights[i];
        }
        
        int mod = sum % 10;
        int checkDigit = 10 - mod;
        if (checkDigit == 10) {
            checkDigit = 0;
        }
        
        return String.valueOf(checkDigit);
    }
}
