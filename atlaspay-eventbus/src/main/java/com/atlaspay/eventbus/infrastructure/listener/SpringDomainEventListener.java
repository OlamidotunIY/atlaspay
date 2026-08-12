package com.atlaspay.eventbus.infrastructure.listener;

import com.atlaspay.eventbus.application.command.SaveOutboxMessageCommand;
import com.atlaspay.eventbus.application.usecase.SaveOutboxMessageUseCase;
import com.atlaspay.shared.event.EnvelopedDomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventListener {

    private final SaveOutboxMessageUseCase saveOutboxMessageUseCase;

    public SpringDomainEventListener(SaveOutboxMessageUseCase saveOutboxMessageUseCase) {
        this.saveOutboxMessageUseCase = saveOutboxMessageUseCase;
    }

    @EventListener
    public void handleDomainEvent(EnvelopedDomainEvent<?> envelopedEvent) {
        String topic = getTopicForEvent(envelopedEvent.event().getClass().getSimpleName());
        saveOutboxMessageUseCase.execute(new SaveOutboxMessageCommand(topic, envelopedEvent));
    }

    private String getTopicForEvent(String eventClassName) {
        if (eventClassName.startsWith("Merchant") || eventClassName.startsWith("ApiKey")) {
            return "merchant-events";
        } else if (eventClassName.startsWith("Customer")) {
            return "customer-events";
        } else if (eventClassName.startsWith("Account") || eventClassName.startsWith("SubAccount")) {
            return "account-events";
        } else if (eventClassName.startsWith("Transaction") || eventClassName.startsWith("Transfer")) {
            return "transaction-events";
        } else if (eventClassName.startsWith("Charge")) {
            return "charge-events";
        } else if (eventClassName.startsWith("Ledger")) {
            return "ledger-events";
        } else if (eventClassName.startsWith("Settlement")) {
            return "settlement-events";
        }
        return "system-events";
    }
}
