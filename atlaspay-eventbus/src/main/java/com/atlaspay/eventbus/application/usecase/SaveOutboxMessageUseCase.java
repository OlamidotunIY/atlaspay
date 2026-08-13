package com.atlaspay.eventbus.application.usecase;

import com.atlaspay.eventbus.application.command.SaveOutboxMessageCommand;
import com.atlaspay.eventbus.domain.model.OutboxMessage;
import com.atlaspay.eventbus.domain.repository.OutboxMessageRepository;
import com.atlaspay.shared.usecase.BaseUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveOutboxMessageUseCase extends BaseUseCase<SaveOutboxMessageCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(SaveOutboxMessageUseCase.class);
    private final OutboxMessageRepository repository;
    private final ObjectMapper objectMapper;

    public SaveOutboxMessageUseCase(OutboxMessageRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Void execute(SaveOutboxMessageCommand command) {
        try {
            String payload = objectMapper.writeValueAsString(command.event());
            OutboxMessage message = new OutboxMessage(
                java.util.UUID.randomUUID().toString(),
                command.topic(),
                payload
            );
            repository.save(message);
            log.debug("Saved event {} to outbox for topic {}", command.event().event().getClass().getSimpleName(), command.topic());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for outbox", e);
            throw new RuntimeException("Failed to serialize event", e);
        }
        return null;
    }
}
