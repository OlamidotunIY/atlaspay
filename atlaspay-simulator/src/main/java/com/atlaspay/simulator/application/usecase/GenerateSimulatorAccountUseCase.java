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
        
        // Note: Realistically, we'd also fire a webhook here to the callbackUrl,
        // but for now we focus on the NUBAN generation part.
        
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
