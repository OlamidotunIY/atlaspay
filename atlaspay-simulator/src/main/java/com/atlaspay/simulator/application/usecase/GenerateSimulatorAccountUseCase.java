package com.atlaspay.simulator.application.usecase;

import com.atlaspay.shared.event.DomainEventPublisher;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import com.atlaspay.shared.event.WebhookDeliveryRequestedEvent;
import com.atlaspay.shared.usecase.BaseUseCase;
import com.atlaspay.simulator.application.command.GenerateSimulatorAccountCommand;
import com.atlaspay.simulator.domain.repository.SimulatorAccountRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class GenerateSimulatorAccountUseCase extends BaseUseCase<GenerateSimulatorAccountCommand, Void> {

    private final SimulatorAccountRepository repository;
    private final DomainEventPublisher publisher;
    private final ObjectMapper objectMapper;

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

        try {
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "event", "virtual_account.created",
                    "data", Map.of(
                            "reference", command.referenceId(),
                            "nuban", nuban
                    )
            ));

            publisher.publish(EnvelopedDomainEvent.wrap(
                    new WebhookDeliveryRequestedEvent(
                            UUID.randomUUID().toString(),
                            command.referenceId(),
                            ZonedDateTime.now(),
                            new WebhookDeliveryRequestedEvent.Payload(
                                    command.callbackUrl(),
                                    payloadJson,
                                    Map.of("X-Simulator-Signature", "dummy")
                            )
                    )
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize webhook payload", e);
        }

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
