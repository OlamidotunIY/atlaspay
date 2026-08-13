package com.atlaspay.accounts.infrastructure.messaging;

import com.atlaspay.shared.event.DomainEvent;
import com.atlaspay.shared.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent<?> event) {
        log.info("Publishing domain event from accounts module: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
