package com.atlaspay.eventbus.infrastructure.scheduler;

import com.atlaspay.eventbus.application.command.ProcessOutboxMessagesCommand;
import com.atlaspay.eventbus.application.usecase.ProcessOutboxMessagesUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPollingScheduler {

    private final ProcessOutboxMessagesUseCase processOutboxMessagesUseCase;

    public OutboxPollingScheduler(ProcessOutboxMessagesUseCase processOutboxMessagesUseCase) {
        this.processOutboxMessagesUseCase = processOutboxMessagesUseCase;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollOutbox() {
        processOutboxMessagesUseCase.execute(new ProcessOutboxMessagesCommand(100));
    }
}
